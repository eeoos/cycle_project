package capstone.cycle.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @NotNull
    @Size(max = 200)
    private String roadAddress; // 도로명 주소

    @NotNull
    @Size(max = 100)
    private String buildingNumber; // 건물 번호

    @Size(max = 100)
    private String detailAddress; // 상세 주소 (동/호수 등)

    @NotNull
    @Size(max = 10)
    private String zipCode;

    public static Address createDefaultAddress() {
        return new Address(
                "기본 도로명 주소",
                "0",
                "기본 상세 주소",
                "00000"
        );
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (roadAddress != null) sb.append(roadAddress);
        if (buildingNumber != null) sb.append(" ").append(buildingNumber);
        if (detailAddress != null) sb.append(" ").append(detailAddress);
        if (zipCode != null) sb.append(" (").append(zipCode).append(")");
        return sb.toString().trim();
    }
}