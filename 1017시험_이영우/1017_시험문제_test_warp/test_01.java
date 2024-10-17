//공공데이터포털에서제공하는“기상청동네예보서비스” API를호출하세요. (25점)
//단위테스트를통해API를호출하고응답받은결과를출력하세요.
//응답데이터를클래스로변환한후“결과코드/결과메세지/전체결과수＂를출력하세요.

//1. Maven Dependency 추가
<dependencies>
    <!-- Spring Web for RestTemplate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- XML 파싱을 위한 라이브러리 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-xml</artifactId>
    </dependency>

    <!-- JUnit for unit tests -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>


//2. WeatherForecastResponse 클래스 생성

public class WeatherForecastResponse {
    private String resultCode;
    private String resultMsg;
    private String totalCount;

    public WeatherForecastResponse(String resultCode, String resultMsg, String totalCount) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.totalCount = totalCount;
    }

    // Getter와 toString 메소드
    public String getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public String getTotalCount() {
        return totalCount;
    }

    @Override
    public String toString() {
        return "Result Code: " + resultCode + ", Result Message: " + resultMsg + ", Total Count: " + totalCount;
    }
}


//3. 기상청 API 호출 서비스 작성
//RestTemplate을 사용하여 API를 호출하고 XML 응답을 파싱하는 서비스를 작성합니다.


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
public class WeatherForecastService {

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

        // XML 파싱
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(response.getBytes()));

        // 결과 코드, 메시지, 전체 결과 수 추출
        String resultCode = getNodeValue(doc, "resultCode");
        String resultMsg = getNodeValue(doc, "resultMsg");
        String totalCount = getNodeValue(doc, "totalCount");

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

//4. 단위 테스트 작성
//이제 WeatherForecastService가 제대로 동작하는지 확인하기 위한 단위 테스트를 작성합니다.
//

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WeatherForecastServiceTest {

    @Autowired
    private WeatherForecastService weatherForecastService;

    @Test
    public void testGetWeatherForecast() throws Exception {
        String nx = "60";  // 서울 좌표
        String ny = "127";  // 서울 좌표

        WeatherForecastResponse response = weatherForecastService.getWeatherForecast(nx, ny);
        assertNotNull(response);
        System.out.println(response);
    }
}
