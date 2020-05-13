package wooteco.subway.admin.acceptance;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import wooteco.subway.admin.dto.LineResponse;
import wooteco.subway.admin.dto.ShortestPathResponse;
import wooteco.subway.admin.dto.StationResponse;

public class ShortestPathAcceptanceTest extends AcceptanceTest {
	@Test
	void name() {
		//given
		StationResponse stationResponse1 = createStation(STATION_NAME_KANGNAM);
		StationResponse stationResponse2 = createStation(STATION_NAME_YEOKSAM);
		StationResponse stationResponse3 = createStation(STATION_NAME_SEOLLEUNG);
		StationResponse stationResponse4 = createStation(STATION_NAME_SADANG);
		StationResponse stationResponse5 = createStation(STATION_NAME_JAMSIL);

		LineResponse lineTwoResponse = createLine(LINE_NAME_2);
		LineResponse lineThreeResponse = createLine(LINE_NAME_3);
		List<LineResponse> lineResponses = Arrays.asList(lineTwoResponse, lineThreeResponse);

		addLineStation(lineTwoResponse.getId(), null, stationResponse1.getId(), 0);
		addLineStation(lineTwoResponse.getId(), stationResponse1.getId(), stationResponse2.getId(), 5);
		addLineStation(lineTwoResponse.getId(), stationResponse2.getId(), stationResponse3.getId(), 5);
		addLineStation(lineTwoResponse.getId(), stationResponse3.getId(), stationResponse4.getId(), 3);

		addLineStation(lineThreeResponse.getId(), null, stationResponse4.getId(), 0);
		addLineStation(lineThreeResponse.getId(), stationResponse4.getId(), stationResponse2.getId(), 9);
		addLineStation(lineThreeResponse.getId(), stationResponse2.getId(), stationResponse5.getId(), 3);

		//when
		ShortestPathResponse shortestPath = findShortestPath(STATION_NAME_KANGNAM, STATION_NAME_SADANG);
		//then
		assertThat(shortestPath.getStations()).containsExactly(
			stationResponse1,
			stationResponse2,
			stationResponse3,
			stationResponse4
		);
		assertThat(shortestPath.getDistance()).isEqualTo(13);
	}

	ShortestPathResponse findShortestPath(String source, String target) {
		return
			given().
				when().
				get("/paths?source=" + source + "&target=" + target).
				then().
				log().all().
				statusCode(HttpStatus.OK.value()).
				extract().as(ShortestPathResponse.class);
	}

}
