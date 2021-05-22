package wooteco.subway.domain;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import wooteco.exception.badrequest.InvalidSectionOnLineException;
import wooteco.exception.notfound.NotFoundDataException;

public class Sections {

    private static final int MINIMUM_COUNT = 1;

    private final List<Section> sections;

    public Sections(List<Section> sections) {
        this.sections = sections;
        validateSectionCount(this.sections);
    }

    private void validateSectionCount(List<Section> sections) {
        if (sections.isEmpty()) {
            throw new IllegalStateException("구간의 수가 너무 적습니다.");
        }
    }

    public boolean isBothEndSection(Section section) {
        Deque<Station> stations = sortedStations();
        return section.isMatchDownStation(stations.peekFirst())
            || section.isMatchUpStation(stations.peekLast());
    }

    public boolean isBothEndStation(Station station) {
        Deque<Station> stations = sortedStations();
        return station.equals(stations.peekFirst())
            || station.equals(stations.peekLast());
    }

    public Deque<Station> sortedStations() {
        Deque<Station> stations = new ArrayDeque<>();
        Map<Station, Station> upStationIds = new LinkedHashMap<>();
        Map<Station, Station> downStationIds = new LinkedHashMap<>();

        initStations(stations, upStationIds, downStationIds);
        sortStations(stations, upStationIds, downStationIds);
        return new ArrayDeque<>(stations);
    }

    private void initStations(Deque<Station> stations, Map<Station, Station> upStations,
        Map<Station, Station> downStations) {

        for (Section section : sections) {
            upStations.put(section.getUpStation(), section.getDownStation());
            downStations.put(section.getDownStation(), section.getUpStation());
        }

        Section section = sections.get(0);
        stations.addFirst(section.getUpStation());
        stations.addLast(section.getDownStation());
    }

    private void sortStations(Deque<Station> stations, Map<Station, Station> upStations,
        Map<Station, Station> downStations) {

        while (upStations.containsKey(stations.peekLast())) {
            Station tailStation = stations.peekLast();
            stations.addLast(upStations.get(tailStation));
        }

        while (downStations.containsKey(stations.peekFirst())) {
            Station headStation = stations.peekFirst();
            stations.addFirst(downStations.get(headStation));
        }
    }

    public void validateInsertable(Section section) {
        boolean isUpStationExisted = isNotExistOnLine(section.getUpStation());
        boolean isDownStationExisted = isNotExistOnLine(section.getDownStation());

        if (isUpStationExisted == isDownStationExisted) {
            throw new InvalidSectionOnLineException();
        }
    }

    public void validateDeletableCount() {
        if (sections.size() <= MINIMUM_COUNT) {
            throw new IllegalStateException("구간을 제거할 수 없습니다.");
        }
    }

    public void validateExistStation(Station station) {
        if (isNotExistOnLine(station)) {
            throw new NotFoundDataException();
        }
    }

    private boolean isNotExistOnLine(Station station) {
        return sections.stream()
            .noneMatch(section -> section.hasSameStation(station));
    }

    public boolean isNotEmpty() {
        return !sections.isEmpty();
    }

    public Section findByMatchStation(Section section) {
        return sections.stream()
            .filter(it -> section.isMatchUpStation(it.getUpStation())
                || section.isMatchDownStation(it.getDownStation()))
            .findAny()
            .orElseThrow(InvalidSectionOnLineException::new);
    }
}
