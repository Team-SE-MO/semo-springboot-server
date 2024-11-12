package sandbox.semo.application.common.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.common.service.CommonService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/common")
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/time")
    public ApiResponse<LocalDateTime> fetchDatabaseServerTime() {
        LocalDateTime data = commonService.fetchDatabaseServerTime();
        return ApiResponse.successResponse(OK, "데이터베이스 서버 시간을 조회 하였습니다.", data);
    }

}
