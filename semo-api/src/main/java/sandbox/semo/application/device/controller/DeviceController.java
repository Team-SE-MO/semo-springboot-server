package sandbox.semo.application.device.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.device.service.DeviceService;
import sandbox.semo.domain.device.dto.request.DeviceRegister;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/hc")
    public ApiResponse<Boolean> testConnect(@RequestBody DeviceRegister deviceRegister) {
        deviceService.healthCheck(deviceRegister);
        return ApiResponse.successResponse(OK, "DEVICE 상태가 양호 합니다.");
    }

}
