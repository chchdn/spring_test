//API 호출 처리를 위한 Spring Batch 작업을 생성하세요. (30점)
//다음과같이작업단계를구성하세요.
// Step1. 기상청 동네예보 서비스 API를 호출하여 데이터를가져오고, 
//응답데이터를ExecutionContext에 저장하세요.

//
//1. Spring Batch 기본 설정
//먼저 Spring Batch를 사용하려면 의존성을 추가해야 합니다. pom.xml 파일에 필요한 의존성을 추가합니다.

<dependencies>
    <!-- Spring Batch -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>

    <!-- Spring Web (API 호출) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 Database (Test용) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>

//
//2. ApiResult 엔티티 클래스 생성
//ApiResult 엔티티는 기상청 API 응답 데이터를 저장하는 엔티티입니다.

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ApiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resultCode;
    private String resultMessage;
    private String totalCount;
    private LocalDateTime requestTime;

    public ApiResult() {}

    public ApiResult(String resultCode, String resultMessage, String totalCount) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.totalCount = totalCount;
        this.requestTime = LocalDateTime.now();
    }

    // Getter 및 Setter 생략
}


//3. Spring Batch Configuration
//배치 작업은 여러 단계로 구성됩니다. 이 단계들을 설정하는 BatchConfig 클래스를 작성합니다.

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job weatherApiJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return jobBuilderFactory.get("weatherApiJob")
                .start(apiCallStep()) // Step 1
                .next(processApiResponseStep()) // Step 2
                .next(saveApiResultStep()) // Step 3
                .build();
    }

    @Bean
    public Step apiCallStep() {
        return stepBuilderFactory.get("apiCallStep")
                .tasklet(apiCallTasklet())
                .build();
    }

    @Bean
    public Step processApiResponseStep() {
        return stepBuilderFactory.get("processApiResponseStep")
                .tasklet(processApiResponseTasklet())
                .build();
    }

    @Bean
    public Step saveApiResultStep() {
        return stepBuilderFactory.get("saveApiResultStep")
                .tasklet(saveApiResultTasklet())
                .build();
    }

    // Step 1: API 호출 및 응답 저장
    @Bean
    public Tasklet apiCallTasklet() {
        return (contribution, chunkContext) -> {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();

            // API 호출
            RestTemplate restTemplate = new RestTemplate();
            String nx = "60"; // 서울 좌표
            String ny = "127";
            String serviceKey = "YOUR_SERVICE_KEY";
            String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=10&pageNo=1&dataType=XML"
                    + "&base_date=20231017&base_time=0600"
                    + "&nx=" + nx
                    + "&ny=" + ny;

            String response = restTemplate.getForObject(url, String.class);

            // API 응답을 ExecutionContext에 저장
            executionContext.put("apiResponse", response);
            System.out.println("API 응답 저장: " + response);

            return RepeatStatus.FINISHED;
        };
    }

    // Step 2: 응답 데이터를 객체로 변환
    @Bean
    public Tasklet processApiResponseTasklet() {
        return (contribution, chunkContext) -> {
            ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            String apiResponse = (String) executionContext.get("apiResponse");

            // 응답 데이터에서 필요한 정보 추출 (예: XML 파싱)
            // 여기서는 간단한 문자열 처리로 가정합니다.
            String resultCode = "0000"; // 추출된 결과 코드
            String resultMessage = "Success"; // 추출된 메시지
            String totalCount = "100"; // 추출된 전체 결과 수

            // 추출한 데이터를 ExecutionContext에 저장
            executionContext.put("resultCode", resultCode);
            executionContext.put("resultMessage", resultMessage);
            executionContext.put("totalCount", totalCount);

            System.out.println("결과 코드: " + resultCode + ", 결과 메시지: " + resultMessage + ", 전체 결과 수: " + totalCount);

            return RepeatStatus.FINISHED;
        };
    }

    // Step 3: ApiResult 엔티티 생성 및 저장
    @Bean
    public Tasklet saveApiResultTasklet() {
        return (contribution, chunkContext) -> {
            ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();

            String resultCode = (String) executionContext.get("resultCode");
            String resultMessage = (String) executionContext.get("resultMessage");
            String totalCount = (String) executionContext.get("totalCount");

            // ApiResult 엔티티 생성 및 저장
            ApiResult apiResult = new ApiResult(resultCode, resultMessage, totalCount);

            // ApiResultRepository를 사용하여 엔티티 저장
            ApiResultRepository apiResultRepository = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                .get("apiResultRepository", ApiResultRepository.class);
            
            apiResultRepository.save(apiResult);

            System.out.println("ApiResult 저장 완료: " + apiResult);

            return RepeatStatus.FINISHED;
        };
    }
}

//4. ApiResultRepository 생성
//ApiResult 엔티티를 데이터베이스에 저장하기 위해 리포지토리 인터페이스를 정의합니다.
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiResultRepository extends JpaRepository<ApiResult, Long> {
}

//5. 실행 이력 제거 (batch_table_drop.txt)
//Spring Batch 작업이 실행된 후에 이력이 남지 않도록 Spring Batch에서 사용하는 테이블을 삭제하는 SQL 스크립트를 작성합니다. 이는 테스트할 때 유용합니다.

DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP SEQUENCE IF EXISTS BATCH_JOB_SEQ;
DROP SEQUENCE IF EXISTS BATCH_STEP_SEQ;


//6. Application 클래스
//Spring Boot 애플리케이션 클래스에서 배치 작업을 실행합니다
//
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherBatchApplication implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job weatherApiJob;

    public static void main(String[] args) {
        SpringApplication.run(WeatherBatchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Job 실행
        jobLauncher.run(weatherApiJob, new org.springframework.batch.core.JobParameters());
    }
}

//7. Step 2 추가 (선택)
//API 응답을 처리하는 두 번째 단계를 추가하려면 BatchConfig에서 새로운 
//스텝을 추가하고 Job에 연결하면 됩니다.
@Bean
public Job weatherApiJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return jobBuilderFactory.get("weatherApiJob")
            .start(weatherApiStep())       // Step 1: API 호출
            .next(processApiResponseStep()) // Step 2: 응답 처리
            .build();
}

@Bean
public Step processApiResponseStep() {
    return stepBuilderFactory.get("processApiResponseStep")
            .tasklet(processApiResponseTasklet())
            .build();
}

//8. 결론
//Step1에서 기상청 동네예보 API를 호출하고, 그 응답을 ExecutionContext에 저장합니다.
//데이터를 ExecutionContext에 저장하면 이후 단계에서 이를 쉽게 재사용할 수 있습니다.
//배치 작업은 쉽게 확장할 수 있으며, Step을 추가하여 다양한 작업 흐름을 만들 수 있습니다.
//이제 Spring Batch가 기상청 동네예보 API를 호출하여 데이터를 처리하고 저장할 수 있도록 구성되었습니다

