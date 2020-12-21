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

    @JsonProperty("pname_no_special_char")
    private String pNamePreprocessingResult;

    @JsonProperty("cname")
    private String categoryName;

    @JsonProperty("cname_preprocessing_result")
    private String cNamePreprocessingResult;

    @JsonProperty("bname")
    private String brandName;

    @JsonProperty("is_exist_bname")
    private Integer isExistBrandName; // true -> 1, false -> 0

    @JsonProperty("kor_bname")
    private String korBNamePreprocessingResult;

    @JsonProperty("eng_bname")
    private String engBNamePreprocessingResult;

    @JsonProperty("view_count")
    private Long viewCount;
}
