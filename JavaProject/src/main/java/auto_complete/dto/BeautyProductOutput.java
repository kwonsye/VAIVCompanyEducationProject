package auto_complete.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class BeautyProductOutput {
    @JsonProperty("pid")
    private Long productId;

    @JsonProperty("pname")
    private String productName;

    @JsonProperty("pname_keystroke_no_spacing")
    private String noSpacingPNameKeystroke;

    @JsonProperty("pname_keystroke_original_spacing")
    private String originalSpacingPNameKeystroke;

    @JsonProperty("cname")
    private String categoryName;

    @JsonProperty("bname")
    private String brandName;

    @JsonProperty("is_exist_bname")
    private Integer isExistBrandName; // True, False

    @JsonProperty("kor_bname_keystroke_no_spacing")
    private String noSpacingKorBNameKeystroke;

    @JsonProperty("eng_bname_lowercase_no_spacing")
    private String noSpacingEngLowercaseBName;

    @JsonProperty("view_count")
    private Long viewCount;
}
