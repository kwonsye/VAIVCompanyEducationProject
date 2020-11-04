package auto_complete.util;

import java.util.HashMap;
import java.util.Map;

public class UnicodeUtil {

    //초성
    public static final String[] CHOSHUNG_ENG_MAPPING = {"r", "R", "s", "e", "E",
            "f", "a", "q", "Q", "t", "T", "d", "w",
            "W", "c", "z", "x", "v", "g"};

    //중성
    public static final String[] JUNGSUNG_ENG_MAPPING = {"k", "o", "i", "O",
            "j", "p", "u", "P", "h", "hk", "ho", "hl",
            "y", "n", "nj", "np", "nl", "b", "m", "ml",
            "l"};

    //종성
    public static final String[] JONGSUNG_ENG_MAPPING = {"", "r", "R", "rt",
            "s", "sw", "sg", "e", "f", "fr", "fa", "fq",
            "ft", "fx", "fv", "fg", "a", "q", "qt", "t",
            "T", "d", "w", "c", "z", "x", "v", "g"};
    public static final int SINGLE_JAUM_BASE_UNICODE = 12593;
    public static final int SINGLE_MOUM_BASE_UNICODE = 12623;

    //단일 자음
    public static String[] SINGLE_JAUM_ENG_MAPPING = {"r", "R", "rt",
            "s", "sw", "sg", "e", "E", "f", "fr", "fa", "fq",
            "ft", "fx", "fv", "fg", "a", "q", "Q", "qt", "t",
            "T", "d", "w", "W", "c", "z", "x", "v", "g"};

    private static final Map<Character, Integer> ROMAN_TO_INTEGER_MAPPING = new HashMap<Character, Integer>() {{
        put('Ⅰ', 1);
        put('Ⅱ', 2);
        put('Ⅲ', 3);
        put('Ⅳ', 4);
        put('Ⅴ', 5);
        put('Ⅵ', 6);
        put('Ⅶ', 7);
    }};

    public static String convertLetterToEngKeystroke(String koreanText) {
        StringBuilder keystrokeBuilder = new StringBuilder();

        for (int charIndex = 0; charIndex < koreanText.length(); charIndex++) {

            char koreanChar = (char) (koreanText.charAt(charIndex) - 0xAC00);

            //자음과 모음이 합쳐진 글자
            if (isCompleteKoreanLetter(koreanChar)) {

                int choSung = koreanChar / (21 * 28);
                int jungSung = koreanChar % (21 * 28) / 28;
                int jongSung = koreanChar % (21 * 28) % 28;

                keystrokeBuilder.append(CHOSHUNG_ENG_MAPPING[choSung]);
                keystrokeBuilder.append(JUNGSUNG_ENG_MAPPING[jungSung]);

                if (jongSung != 0x0000) { // 종성이 존재할 경우
                    keystrokeBuilder.append(JONGSUNG_ENG_MAPPING[jongSung]);
                }
            } else {
                if (isSingleJaum(koreanChar)) { // 단일 자음
                    int jaum = (koreanChar - 34097);
                    keystrokeBuilder.append(SINGLE_JAUM_ENG_MAPPING[jaum]);
                } else if (isSingleMoum(koreanChar)) { //단일 모음
                    int moum = (koreanChar - 34127);
                    keystrokeBuilder.append(JUNGSUNG_ENG_MAPPING[moum]);
                } else {
                    char originalChar = (char) (koreanChar + 0xAC00);

                    if (isAlphabet(originalChar)) {
                        keystrokeBuilder.append(String.valueOf(originalChar).toLowerCase());
                    }

                    if (isRomanCharacter(originalChar)) {
                        keystrokeBuilder.append(convertRomanToInteger(originalChar));
                    }

                    if (isNumericCharacter(originalChar)) {
                        keystrokeBuilder.append(originalChar);
                    }

                    if (isSpace(originalChar)) {
                        keystrokeBuilder.append(originalChar);
                    }
                }
            }
        }
        return keystrokeBuilder.toString();
    }

    public static String removeSpecialCharacter(String text) {

        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            char singleChar = text.charAt(charIndex);

            if (isRomanCharacter(singleChar)) {
                text.replace(singleChar, (char) (convertRomanToInteger(singleChar) + '0'));
            }
        }

        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        return text.replaceAll(match, "");
    }


    public static int getAlphabetCharacterCountOfString(String text) {
        int alphabetCount = 0;

        for (char character : text.toCharArray()) {
            if (isAlphabet(character)) {
                alphabetCount++;
            }
        }
        return alphabetCount;
    }

    private static boolean isCompleteKoreanLetter(char koreanChar) {
        return (koreanChar >= 0 && koreanChar <= 11172);
    }

    private static boolean isSingleMoum(char koreanChar) {
        return (koreanChar >= 34127 && koreanChar <= 34147);
    }

    private static boolean isSingleJaum(char koreanChar) {
        return (koreanChar >= 34097 && koreanChar <= 34126);
    }

    public static boolean isAlphabet(char character) {
        return ((character >= 65 && character <= 90) || (character >= 97 && character <= 122));
    }

    private static boolean isSpace(char originalChar) {
        return originalChar == ' ';
    }

    private static boolean isNumericCharacter(char character) {
        return (character >= 48 && character <= 57);
    }

    private static boolean isRomanCharacter(char character) {
        return ROMAN_TO_INTEGER_MAPPING.keySet().contains(character);
    }

    private static int convertRomanToInteger(char romanCharacter) {
        return ROMAN_TO_INTEGER_MAPPING.get(romanCharacter);
    }

    public static String convertToKoreanLetter(int choSungIndex, int jungSungIndex, int jongSungIndex) {
        char koreanLetter = (char) (0xAC00 + 28 * 21 * (choSungIndex) + 28 * (jungSungIndex) + (jongSungIndex));
        return String.valueOf(koreanLetter);
    }

    public static String convertToEngKeystroke(int choSungIndex, int jungSungIndex, int jongSungIndex) {
        return CHOSHUNG_ENG_MAPPING[choSungIndex] + JUNGSUNG_ENG_MAPPING[jungSungIndex] + JONGSUNG_ENG_MAPPING[jongSungIndex];
    }
}
