//1. ApiResultRepository 생성
//먼저, JPA의 JpaRepository를 상속하여 ApiResultRepository 인터페이스를 생성합니다.
//이 인터페이스는 자동으로 CRUD(Create, Read, Update, Delete) 기능을 제공

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiResultRepository extends JpaRepository<ApiResult, Long> {
}



//2. CRUD 기능 테스트
//이제 ApiResultRepository를 사용하여 데이터 조회, 추가, 수정, 삭제 기능을 테스트하는 코드를 작성합니다. 이 테스트는 JUnit 5를 사용하며, 
//스프링 부트에서 제공하는 @SpringBootTest를 사용하여 통합 테스트로 실행


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApiResultRepositoryTest {

    @Autowired
    private ApiResultRepository apiResultRepository;

    @Test
    public void testAddApiResult() {
        // 데이터 추가 (Create)
        ApiResult apiResult = new ApiResult("0000", "Success", "100");
        ApiResult savedResult = apiResultRepository.save(apiResult);

        // 추가된 데이터가 저장되었는지 확인
        assertNotNull(savedResult.getId());
        assertEquals("0000", savedResult.getResultCode());
        System.out.println("Saved ApiResult: " + savedResult);
    }

    @Test
    public void testGetApiResult() {
        // 데이터 조회 (Read)
        ApiResult apiResult = new ApiResult("0000", "Success", "100");
        ApiResult savedResult = apiResultRepository.save(apiResult);

        // ID를 사용하여 데이터 조회
        Optional<ApiResult> foundResult = apiResultRepository.findById(savedResult.getId());
        assertTrue(foundResult.isPresent());
        assertEquals("Success", foundResult.get().getResultMsg());
        System.out.println("Found ApiResult: " + foundResult.get());
    }

    @Test
    public void testUpdateApiResult() {
        // 데이터 수정 (Update)
        ApiResult apiResult = new ApiResult("0000", "Success", "100");
        ApiResult savedResult = apiResultRepository.save(apiResult);

        // 수정할 데이터 가져오기
        Optional<ApiResult> resultToUpdate = apiResultRepository.findById(savedResult.getId());
        assertTrue(resultToUpdate

        		
//        		3. 각 테스트의 설명
//        		데이터 추가 (Create):
//
//        		apiResultRepository.save()를 사용하여 ApiResult 객체를 저장합니다.
//        		저장 후 반환된 객체를 통해 ID가 자동 생성되었는지 확인합니다.
//        		데이터 조회 (Read):
//
//        		apiResultRepository.findById()를 사용하여 ID를 기반으로 데이터를 조회합니다.
//        		Optional 객체를 사용하여 데이터가 존재하는지 여부를 확인합니다.
//        		데이터 수정 (Update):
//
//        		먼저 데이터를 조회한 후 수정할 부분을 변경하고 다시 apiResultRepository.save()를 호출하여 수정된 데이터를 저장합니다.
//        		수정된 결과를 다시 조회하여 업데이트가 정상적으로 이루어졌는지 확인합니다.
//        		데이터 삭제 (Delete):
//
//        		apiResultRepository.delete()를 사용하여 데이터를 삭제합니다.
//        		삭제 후 해당 데이터가 존재하지 않는지 확인합니다.
//        		4. 실행 방법
//        		테스트는 @SpringBootTest로 통합 테스트 환경에서 실행되므로, 스프링 부트 애플리케이션을 실행하는 것처럼 테스트가 실행됩니다.
//        		mvn test 또는 IDE에서 테스트 파일을 실행하여 각 기능이 정상적으로 동작하는지 확인할 수 있습니다.
//        		5. 전체 동작 흐름 요약
//        		데이터 추가: save() 메서드를 통해 ApiResult 객체를 추가합니다.
//        		데이터 조회: findById() 메서드를 통해 저장된 데이터를 조회합니다.
//        		데이터 수정: 먼저 데이터를 조회한 후, 수정된 데이터를 save()로 다시 저장합니다.
//        		데이터 삭제: delete() 메서드를 사용하여 데이터를 삭제한 후, 조회 시 데이터가 존재하지 않는지 확인합니다.
//        		이로써 ApiResultRepository 인터페이스를 사용하여 CRUD 기능을 성공적으로 테스트할 수 있습니다       		
//        		
//        		
//        		