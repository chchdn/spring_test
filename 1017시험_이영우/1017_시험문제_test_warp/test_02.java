//1. ApiResult 엔티티 클래스 생성

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_result")
public class ApiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 자동 증가 번호

    @Column(name = "result_code", nullable = false)
    private String resultCode;

    @Column(name = "result_msg", nullable = false)
    private String resultMsg;

    @Column(name = "total_count", nullable = false)
    private String totalCount;

    @Column(name = "api_call_time", nullable = false)
    private LocalDateTime apiCallTime; // API 호출 시간

    public ApiResult() {
        this.apiCallTime = LocalDateTime.now(); // 현재 시간 자동 설정
    }

    public ApiResult(String resultCode, String resultMsg, String totalCount) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.totalCount = totalCount;
        this.apiCallTime = LocalDateTime.now(); // 생성 시점에 현재 시간 자동 저장
    }

    // Getters 및 Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public LocalDateTime getApiCallTime() {
        return apiCallTime;
    }

    public void setApiCallTime(LocalDateTime apiCallTime) {
        this.apiCallTime = apiCallTime;
    }
}

//2. ApiResultRepository 인터페이스 생성
//API 호출 결과를 데이터베이스에 저장하고 조회하기 위해 JPA Repository를 생성
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiResultRepository extends JpaRepository<ApiResult, Long> {
}

//3. API 호출 시 데이터 저장 로직 추가
//WeatherForecastService에서 API 호출 후 받은 데이터를 ApiResult 엔티티로 변환하여 저장하는 로직을 추가
//
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherForecastService {

    @Autowired
    private ApiResultRepository apiResultRepository;

    private final String serviceKey = "YOUR_SERVICE_KEY";

    public WeatherForecastResponse getWeatherForecast(String nx, String ny) throws Exception {
        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=10&pageNo=1&dataType=XML"
                + "&base_date=20231017&base_time=0600"
                + "&nx=" + nx
                + "&ny=" + ny;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        // XML 파싱 및 ApiResult 엔티티 생성
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(response.getBytes()));

        String resultCode = getNodeValue(doc, "resultCode");
        String resultMsg = getNodeValue(doc, "resultMsg");
        String totalCount = getNodeValue(doc, "totalCount");

        // API 호출 결과를 ApiResult 엔티티로 저장
        ApiResult apiResult = new ApiResult(resultCode, resultMsg, totalCount);
        apiResultRepository.save(apiResult); // DB에 저장

        return new WeatherForecastResponse(resultCode, resultMsg, totalCount);
    }

    private String getNodeValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}

//4. 데이터베이스 설정
//Spring Boot는 기본적으로 H2, MySQL 등 여러 데이터베이스를 지원합니다. application.properties 파일에서 데이터베이스 설정을 추가
//
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true


//5. 테이블 자동 생성
//Spring Boot는 JPA를 통해 애플리케이션 시작 시 엔티티에 기반하여 자동으로 테이블을 생성합니다. spring.jpa.hibernate.ddl-auto=update 설정을 통해 엔티티에 해당하는 테이블이 자동으로 생성


//6. 테스트 코드 작성
//API 호출 후 ApiResult 엔티티가 제대로 저장되는지 테스트 코드를 작성
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WeatherForecastServiceTest {

    @Autowired
    private WeatherForecastService weatherForecastService;

    @Autowired
    private ApiResultRepository apiResultRepository;

    @Test
    public void testGetWeatherForecast() throws Exception {
        String nx = "60";  // 서울 좌표
        String ny = "127";  // 서울 좌표

        WeatherForecastResponse response = weatherForecastService.getWeatherForecast(nx, ny);
        assertNotNull(response);

        // ApiResult가 제대로 저장되었는지 확인
        ApiResult lastResult = apiResultRepository.findAll().get(0);
        assertEquals(response.getResultCode(), lastResult.getResultCode());
        System.out.println(lastResult);
    }
}



