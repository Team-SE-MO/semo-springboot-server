package sandbox.semo.application.device.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.device.service.DeviceService;
import sandbox.semo.application.security.authentication.JwtMemberDetails;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;
import sandbox.semo.domain.device.dto.request.DeviceUpdate;
import sandbox.semo.domain.device.dto.response.DeviceInfo;

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
            @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        deviceService.register(memberDetails.getCompanyId(), request);
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 등록되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<DeviceInfo>> getDeviceInfoByCompany(
            @AuthenticationPrincipal JwtMemberDetails memberDetails) {
        List<DeviceInfo> data = deviceService.getDeviceInfo(
                memberDetails.getRole(),
                memberDetails.getCompanyId()
        );
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 조회 되었습니다.", data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public ApiResponse<Void> updateDeviceInfo(
            @Valid @RequestBody DeviceUpdate request,
            @AuthenticationPrincipal JwtMemberDetails memberDetails) {
        deviceService.update(
                memberDetails.getCompanyId(),
                request
        );
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 수정 되었습니다.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ApiResponse<Void> deleteDevice(
            @RequestParam String deviceAlias,
            @AuthenticationPrincipal JwtMemberDetails memberDetails) {
        deviceService.deleteDevice(memberDetails.getCompanyId(), deviceAlias);
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 삭제 되었습니다.");
    }

}
