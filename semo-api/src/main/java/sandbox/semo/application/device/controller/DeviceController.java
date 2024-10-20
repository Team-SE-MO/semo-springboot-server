package sandbox.semo.application.device.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.device.service.DeviceService;
import sandbox.semo.application.security.authentication.MemberPrincipalDetails;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/hc")
    public ApiResponse<Boolean> testConnect(@Valid @RequestBody DataBaseInfo request) {
        boolean status = deviceService.healthCheck(request);
        return ApiResponse.successResponse(OK, "DEVICE 상태가 양호 합니다.", status);
    }

    @PreAuthorize("hasAnyRole('SUPER', 'ADMIN')")
    @PostMapping
    public ApiResponse<Void> register(
            @RequestBody DeviceRegister request,
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails
    ) {
        deviceService.register(memberDetails.getMember().getCompany(), request);
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 등록되었습니다.");
    }

}
