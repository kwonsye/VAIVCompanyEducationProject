package auto_complete.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BeautyProductInput {

    @JsonProperty("pid")
    private Long productId;

    @JsonProperty("pname")
    private String productName;

    @JsonProperty("cname")
    private String categoryName;

    @JsonProperty("bname")
    private String brandName;

    @JsonProperty("view_count")
    private Long viewCount;
}
