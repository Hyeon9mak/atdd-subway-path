package wooteco.subway.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.exception.notfound.NotFoundDataException;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.StationServiceDto;

@Service
public class StationService {

    private static final int NOT_FOUND = 0;
    private final StationDao stationDao;

    public StationService(StationDao stationDao) {
        this.stationDao = stationDao;
    }

    public List<StationServiceDto> showStations() {
        List<Station> stations = stationDao.findAll();

        return stations.stream()
            .map(StationServiceDto::from)
            .collect(Collectors.toList());
    }

    public Station findById(Long stationId) {
        return stationDao.findById(stationId);
    }

    @Transactional
    public StationServiceDto save(@Valid StationServiceDto stationServiceDto) {
        Station station = stationServiceDto.toEntity();
        Station saveStation = stationDao.insert(station);

        return StationServiceDto.from(saveStation);
    }

    @Transactional
    public void delete(@Valid StationServiceDto stationServiceDto) {
        if (stationDao.delete(stationServiceDto.getId()) == NOT_FOUND) {
            throw new NotFoundDataException();
        }
    }
}
