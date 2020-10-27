package auto_complete;

import auto_complete.dto.BeautyProductInput;
import auto_complete.dto.BeautyProductOutput;
import auto_complete.util.StringUtil;
import auto_complete.util.UnicodeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class AutoComplete {

    private final static String DATA_FILE_PATH = "product.json";
    private final static String OUTPUT_FILE_PATH = "preprocessing_result.json";

    public static void main(String[] args) throws IOException {
        preprocessing();
    }

    private static void preprocessing() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH));
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_PATH));
        String json;
        ObjectMapper jsonParser = new ObjectMapper();

        while((json = reader.readLine()) != null){
            BeautyProductInput beautyProductInput = jsonParser.readValue(json, BeautyProductInput.class);

            String spacingPNameKeystroke = UnicodeUtil.convertToEngKeystroke(beautyProductInput.getProductName());
            String noSpacingPNameKeystroke = StringUtil.convertToNoSpacing(spacingPNameKeystroke);

            boolean isExistBrandName = isExistBrandName(isExistBrandName(beautyProductInput.getBrandName()));
            String noSpacingBNameKeystroke = isExistBrandName ?
                    UnicodeUtil.convertToEngKeystroke(getKoreanBrandName(beautyProductInput.getBrandName())) : "";
            String noSpacingEngLowercaseBName = isExistBrandName ?
                    UnicodeUtil.convertToEngKeystroke(getEngLowercaseBrandName(beautyProductInput.getBrandName())): "";

            BeautyProductOutput output = BeautyProductOutput.builder()
                    .productId(beautyProductInput.getProductId())
                    .productName(beautyProductInput.getProductName())
                    .noSpacingPNameKeystroke(noSpacingPNameKeystroke)
                    .originalSpacingPNameKeystroke(spacingPNameKeystroke)
                    .categoryName(beautyProductInput.getCategoryName())
                    .brandName(beautyProductInput.getBrandName())
                    .noSpacingKorBNameKeystroke(noSpacingBNameKeystroke)
                    .noSpacingEngLowercaseBName(noSpacingEngLowercaseBName)
                    .viewCount(beautyProductInput.getViewCount())
                    .isExistBrandName(isExistBrandName(beautyProductInput.getBrandName()))
                    .build();

            writer.write(jsonParser.writeValueAsString(output)+"\n");
        }

        reader.close();
        writer.close();
    }

    private static Integer isExistBrandName(String originalBrandName){
        return originalBrandName.equals("unknown") == true ? 0 : 1;
    }

    private static boolean isExistBrandName(int existence){
        return existence == 1 ? true : false;
    }

    private static String getEngLowercaseBrandName(String originalBrandName) {
        String[] splits = StringUtil.splitBracketWithNoSpacing(originalBrandName);

        if(splits.length == 1){
            return "";
        }

        int alphabetCountOfFirstSplit = UnicodeUtil.getAlphabetCharacterCountOfString(splits[0]);
        int alphabetCountOfSecondSplit = UnicodeUtil.getAlphabetCharacterCountOfString(splits[1]);

        if(alphabetCountOfFirstSplit > alphabetCountOfSecondSplit){
            return splits[0].toLowerCase();
        }else{
            return splits[1].toLowerCase();
        }

    }

    private static String getKoreanBrandName(String originalBrandName){
        String[] splits = StringUtil.splitBracketWithNoSpacing(originalBrandName);

        if(splits.length == 1){
            return splits[0];
        }

        int alphabetCountOfFirstSplit = UnicodeUtil.getAlphabetCharacterCountOfString(splits[0]);
        int alphabetCountOfSecondSplit = UnicodeUtil.getAlphabetCharacterCountOfString(splits[1]);

        if(alphabetCountOfFirstSplit > alphabetCountOfSecondSplit){
            return splits[1];
        }else{
            return splits[0];
        }
    }
}

