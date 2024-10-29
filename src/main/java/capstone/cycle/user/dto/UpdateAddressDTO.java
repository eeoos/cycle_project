package capstone.cycle.user.dto;

import capstone.cycle.user.entity.Address;
import jakarta.validation.Valid;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateAddressDTO {

    private String roadAddress;
    private String buildingNumber;
    private String detailAddress;
    private String zipCode;

    public static UpdateAddressDTO of(String roadAddress, String buildingNumber,
                                      String detailAddress, String zipCode) {
        return UpdateAddressDTO.builder()
                .roadAddress(roadAddress)
                .buildingNumber(buildingNumber)
                .detailAddress(detailAddress)
                .zipCode(zipCode)
                .build();
    }
}
