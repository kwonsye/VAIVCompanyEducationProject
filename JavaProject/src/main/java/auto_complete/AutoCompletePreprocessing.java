package auto_complete;

import auto_complete.dto.BeautyProductInput;
import auto_complete.dto.BeautyProductOutput;
import auto_complete.util.StringUtil;
import auto_complete.util.UnicodeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AutoCompletePreprocessing {

    private final static String DATA_FILE_PATH = "product.json";
    private final static String OUTPUT_FILE_PATH = "preprocessing_result.json";
    private final static String OUTPUT_DICTIONARY_FILE_PATH = "keystroke_dictionary.json";

    private final static Integer NO_CORRESPONDING_KOREAN_CHAR_INDEX = 0;

    public static void main(String[] args) throws IOException {
        //preprocessing();
        makeKeystrokeDictionary();
    }

    private static void makeKeystrokeDictionary() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DICTIONARY_FILE_PATH));
        Map<String, String> dictionaryMap = new LinkedHashMap<>();
        ObjectMapper jsonMapper = new ObjectMapper();
        String koreanKey;
        String keystrokeValue;

        for(int singleJaumIndex = 0; singleJaumIndex<UnicodeUtil.SINGLE_JAUM_ENG_MAPPING.length; singleJaumIndex++){
            koreanKey = String.valueOf((char)(UnicodeUtil.SINGLE_JAUM_BASE_UNICODE + singleJaumIndex));
            dictionaryMap.put(koreanKey, UnicodeUtil.SINGLE_JAUM_ENG_MAPPING[singleJaumIndex]);
        }

        for(int singleMoumIndex = 0; singleMoumIndex<UnicodeUtil.JUNGSUNG_ENG_MAPPING.length; singleMoumIndex++){
            koreanKey = String.valueOf((char)(UnicodeUtil.SINGLE_MOUM_BASE_UNICODE + singleMoumIndex));
            dictionaryMap.put(koreanKey, UnicodeUtil.JUNGSUNG_ENG_MAPPING[singleMoumIndex]);
        }

        for(int choSungIndex = 0; choSungIndex < UnicodeUtil.CHOSHUNG_ENG_MAPPING.length; choSungIndex++){

            for(int jungSungIndex = 0; jungSungIndex < UnicodeUtil.JUNGSUNG_ENG_MAPPING.length; jungSungIndex++){

                for(int jongSungIndex = 0; jongSungIndex<UnicodeUtil.JONGSUNG_ENG_MAPPING.length; jongSungIndex++){
                    koreanKey = UnicodeUtil.convertToKoreanLetter(choSungIndex, jungSungIndex, jongSungIndex);
                    keystrokeValue = UnicodeUtil.convertToEngKeystroke(choSungIndex,jungSungIndex, jongSungIndex);

                    dictionaryMap.put(koreanKey, keystrokeValue);
                }
            }
        }

        writer.write(jsonMapper.writeValueAsString(dictionaryMap));
        System.out.println(dictionaryMap.size() == 11172);
        writer.close();
    }

    private static void preprocessing() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH));
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_PATH));
        String json;
        ObjectMapper jsonParser = new ObjectMapper();

        while((json = reader.readLine()) != null){
            BeautyProductInput beautyProductInput = jsonParser.readValue(json, BeautyProductInput.class);

            boolean isExistBrandName = isExistBrandName(isExistBrandName(beautyProductInput.getBrandName()));
            String korBNamePreprocessingResult = isExistBrandName ?
                    UnicodeUtil.removeSpecialCharacter(getKoreanBrandName(beautyProductInput.getBrandName())) : "";
            String engBNamePreprocessingResult = isExistBrandName ?
                    UnicodeUtil.removeSpecialCharacter(getEngLowercaseBrandName(beautyProductInput.getBrandName())): "";

            String noSpecialCharPName = UnicodeUtil.removeSpecialCharacter(beautyProductInput.getProductName());
            String noSpecialCharWithNoSpacingCName = StringUtil.convertToNoSpacing(
                    UnicodeUtil.removeSpecialCharacter(beautyProductInput.getCategoryName()));

            BeautyProductOutput output = BeautyProductOutput.builder()
                    .productId(beautyProductInput.getProductId())
                    .productName(beautyProductInput.getProductName())
                    .pNamePreprocessingResult(noSpecialCharPName)
                    .categoryName(beautyProductInput.getCategoryName())
                    .cNamePreprocessingResult(noSpecialCharWithNoSpacingCName)
                    .brandName(beautyProductInput.getBrandName())
                    .isExistBrandName(isExistBrandName(beautyProductInput.getBrandName()))
                    .korBNamePreprocessingResult(korBNamePreprocessingResult)
                    .engBNamePreprocessingResult(engBNamePreprocessingResult)
                    .viewCount(beautyProductInput.getViewCount())
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

