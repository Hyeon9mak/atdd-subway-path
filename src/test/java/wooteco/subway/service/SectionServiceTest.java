package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Deque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import wooteco.exception.badrequest.InvalidDistanceException;
import wooteco.exception.notfound.NotFoundDataException;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Distance;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.SectionServiceDto;

@Sql("classpath:initializeTable.sql")
@SpringBootTest
public class SectionServiceTest {

    private final Line 일호선 = new Line(1L, "1호선", "bg-blue-100");
    private final Station 신설역 = new Station(1L, "신설역");
    private final Station 동묘역 = new Station(2L, "동묘역");
    private final Station 동대문역 = new Station(3L, "동대문역");
    private final Distance 거리 = new Distance(10);

    @Autowired
    private SectionDao sectionDao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private LineDao lineDao;
    @Autowired
    private SectionService sectionService;

    @BeforeEach
    void setUp() {
        lineDao.insert(일호선);
        stationDao.insert(신설역);
        stationDao.insert(동묘역);
        stationDao.insert(동대문역);

        Section 신설역_동묘역_구간 = new Section(일호선, 신설역, 동묘역, 거리);
        Section 동묘역_동대문역_구간 = new Section(일호선, 동묘역, 동대문역, 거리);
        sectionDao.insert(신설역_동묘역_구간);
        sectionDao.insert(동묘역_동대문역_구간);
    }

    @Test
    @DisplayName("노선의 끝 쪽에 구간 추가")
    void createSectionWithLineAndStations() {
        // given
        Station 회기역 = stationDao.insert(new Station("회기역"));

        SectionServiceDto 동대문_회기_구간_DTO =
            new SectionServiceDto(일호선.getId(), 동대문역.getId(), 회기역.getId(), 거리.getValue());

        // when
        SectionServiceDto 동대문_회기_구간 = sectionService.save(일호선, 동대문_회기_구간_DTO);

        // then
        assertThat(동대문_회기_구간.getLineId()).isEqualTo(일호선.getId());
        assertThat(동대문_회기_구간.getUpStationId()).isEqualTo(동대문역.getId());
        assertThat(동대문_회기_구간.getDownStationId()).isEqualTo(회기역.getId());
        assertThat(동대문_회기_구간.getDistance()).isEqualTo(거리.getValue());
    }

    @Test
    @DisplayName("신설-동묘-동대문 사이에 추가 신설-동묘-회기-동대문")
    void createSectionBetweenSections() {
        // given
        Station 회기역 = stationDao.insert(new Station("회기역"));
        int 목표거리 = 3;

        SectionServiceDto 동묘_회기_구간_DTO =
            new SectionServiceDto(일호선.getId(), 동묘역.getId(), 회기역.getId(), 목표거리);

        // when
        SectionServiceDto 동묘_회기_구간 = sectionService.save(일호선, 동묘_회기_구간_DTO);
        Section 변화된_구간1 = sectionDao.findByLineIdAndDownStationId(일호선.getId(), 회기역.getId()).get();
        Section 변화된_구간2 = sectionDao.findByLineIdAndUpStationId(일호선.getId(), 회기역.getId()).get();

        // then
        assertThat(동묘_회기_구간.getLineId()).isEqualTo(일호선.getId());
        assertThat(동묘_회기_구간.getUpStationId()).isEqualTo(동묘역.getId());
        assertThat(동묘_회기_구간.getDownStationId()).isEqualTo(회기역.getId());
        assertThat(동묘_회기_구간.getDistance()).isEqualTo(목표거리);

        assertThat(변화된_구간1.getUpStationId()).isEqualTo(동묘역.getId());
        assertThat(변화된_구간1.getDownStationId()).isEqualTo(회기역.getId());
        assertThat(변화된_구간1.getDistanceValue()).isEqualTo(3);

        assertThat(변화된_구간2.getUpStationId()).isEqualTo(회기역.getId());
        assertThat(변화된_구간2.getDownStationId()).isEqualTo(동대문역.getId());
        assertThat(변화된_구간2.getDistanceValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("역 사이에 먼 거리 추가")
    void createSectionBetweenSectionsWithExcessDistance() {
        // given
        Station 회기역 = stationDao.insert(new Station("회기역"));

        // when
        SectionServiceDto 역_사이_거리가_기존_구간보다_먼_구간 =
            new SectionServiceDto(일호선.getId(), 동묘역.getId(), 회기역.getId(), 15);

        // then
        assertThatThrownBy(() -> sectionService.save(일호선, 역_사이_거리가_기존_구간보다_먼_구간))
            .isInstanceOf(InvalidDistanceException.class);
    }

    @Test
    @DisplayName("지하철 역이 3개 이상일 때 노선의 중간역 삭제")
    void deleteSection() {
        // given

        // when
        sectionService.delete(일호선, 동묘역.getId());
        Section section = sectionDao.findByLineIdAndUpStationId(일호선.getId(), 신설역.getId())
            .orElseThrow(NotFoundDataException::new);

        // then
        assertThat(section.getUpStation()).isEqualTo(신설역);
        assertThat(section.getDownStation()).isEqualTo(동대문역);
        assertThat(section.getDistanceValue()).isEqualTo(20);
    }

    @Test
    @DisplayName("지하철 역이 3개 이상일 때 노선 끝에 존재하는 역(구간) 삭제")
    void deleteSectionAtEnd() {
        // given

        // when
        Sections beforeSections = new Sections(sectionDao.findAllByLineId(일호선.getId()));
        Deque<Station> 구간삭제_전_정렬된_역들 = beforeSections.sortedStations();
        sectionService.delete(일호선, 동대문역.getId());
        Sections afterSections = new Sections(sectionDao.findAllByLineId(일호선.getId()));
        Deque<Station> 구간삭제_후_정렬된_역들 = afterSections.sortedStations();

        // then
        assertThat(구간삭제_전_정렬된_역들.peekFirst()).isEqualTo(신설역);
        assertThat(구간삭제_전_정렬된_역들.peekLast()).isEqualTo(동대문역);
        assertThat(구간삭제_후_정렬된_역들.peekFirst()).isEqualTo(신설역);
        assertThat(구간삭제_후_정렬된_역들.peekLast()).isEqualTo(동묘역);
    }

    @Test
    @DisplayName("지하철 역이 2개만 있을 때(구간이 1개일 때)의 삭제")
    void deleteSectionWithTwoStations() {
        // given

        // when
        sectionService.delete(일호선, 동묘역.getId());

        // then
        assertThatThrownBy(() -> sectionService.delete(일호선, 신설역.getId()))
            .isInstanceOf(IllegalStateException.class);
    }
}
