// KoreanNLPService.java

package com.reviewgenie.service;

import com.reviewgenie.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KoreanNLPService {

    private final KeywordRepository keywordRepository; // 생성자에서 필요 (현재 미사용이지만 추후 확장 가능)

    // [MODIFIED] 핵심 키워드 리스트
    private static final Set<String> KEY_TERMS = Set.of(
            "맛", "가격", "대기시간", "서비스", "예약", "포장", "청결", "인테리어", "메뉴"
    );

    // [NEW] 키워드별 긍정 감성어 사전
    private static final Map<String, Set<String>> KEYWORD_POSITIVE_WORDS = Map.of(
            "맛", Set.of("맛있", "맛나", "맛좋", "달콤", "고소", "시원", "깔끔", "부드러", "쫄깃", "짱", "환상적", "최고", "좋", "훌륭", "완벽", "신선", "맛집"),
            "인테리어", Set.of("예쁜", "러블리", "깔끔", "세련", "아늑", "분위기좋", "꾸며져", "멋지", "이쁘", "아름다", "고급", "모던", "클래식", "감각적"),
            "서비스", Set.of("친절", "빠르", "정성", "세심", "배려", "좋", "만족", "완벽", "훌륭", "감사", "최고", "괜찮"),
            "가격", Set.of("저렴", "합리적", "괜찮", "적당", "만족", "좋", "가성비", "알맞"),
            "대기시간", Set.of("빠르", "짧", "금방", "바로", "신속", "즉시"),
            "예약", Set.of("편리", "쉽", "간단", "좋", "괜찮", "만족"),
            "포장", Set.of("깔끔", "좋", "완벽", "세심", "괜찮", "만족", "편리"),
            "청결", Set.of("깨끗", "깔끔", "위생적", "청결", "좋", "완벽", "만족"),
            "메뉴", Set.of("다양", "풍부", "좋", "많", "괜찮", "만족", "선택폭")
    );

    // [NEW] 키워드별 부정 감성어 사전
    private static final Map<String, Set<String>> KEYWORD_NEGATIVE_WORDS = Map.of(
            "맛", Set.of("맛없", "아쉬", "별로", "싱거", "짜", "느끼", "질겨", "달", "진해", "밍밍", "시", "쓴", "떫", "비리", "텁텁"),
            "가격", Set.of("비싸", "부담", "아까", "폭리", "과도", "높", "부담스러"),
            "대기시간", Set.of("길어", "오래", "늦", "느리", "답답", "기다려", "지연", "힘들"),
            "서비스", Set.of("불친절", "느리", "불만", "별로", "아쉬", "부족", "나쁘", "실망", "화나"),
            "예약", Set.of("어려", "힘들", "복잡", "불편", "안돼", "막혀"),
            "포장", Set.of("아쉬", "별로", "불편", "부족", "나쁘", "엉성", "허술"),
            "청결", Set.of("더럽", "지저분", "불결", "냄새", "벌레", "먼지", "얼룩"),
            "메뉴", Set.of("적", "부족", "없", "별로", "아쉬", "단조", "제한적")
    );

    // [NEW] 부정 맥락어 (긍정어와 함께 나타나면 부정으로 전환)
    private static final Set<String> NEGATIVE_CONTEXT_WORDS = Set.of(
            "아쉬", "하지만", "그런데", "다만", "그러나", "근데", "안타깝", "아깝", "그럼에도", "차라리", "오히려"
    );

    // [NEW] 키워드에서 제외할 불용어 확장 (명사 위주 추출을 위한 비명사 단어들 제외)
    private static final Set<String> STOP_WORDS = Set.of(
            // 대명사, 수사, 조사 등
            "이", "그", "저", "것", "수", "등", "및", "제", "때", "곳", "중", "내", "외",
            "한", "두", "세", "네", "다섯", "또", "더", "덜", "말", "때문", "위해",
            "통해", "따라", "같이", "함께", "각각", "모든", "여러", "많은", "적은",
            
            // 형용사, 동사, 어미 등 (명사가 아닌 단어들)
            "예쁜", "맛있는", "맛있", "좋은", "최고", "가요", "있어요", "없어요", "입니다", "합니다",
            "진짜", "정말", "너무", "아주", "매우", "조금", "약간", "완전", "거의", "아마",
            "그냥", "그래서", "그런데", "하지만", "그러나", "그리고", "또한", "역시",
            "환상적입니다", "길어서", "추천해요", "곳이에요", "힘들었어요", "좋아서", "편이지만", "만족합니다",
            "깔끔하게", "해주셨어요", "비싸", "특히", "다만", "주말에는", "잘",
            
            // 일반적인 불용어 (의미가 약한 명사들)
            "사람", "분", "씨", "님", "거기", "여기", "저기", "이거", "그거", "저거",
            "이런", "그런", "저런", "어떤", "무슨", "같은", "다른", "새로운"
    );


    
    // [NEW] 명사 패턴 정의 (한글로 이루어진 2글자 이상의 단어)
    private static final Pattern NOUN_PATTERN = Pattern.compile("^[가-힣]{2,}$");
    
    // [NEW] 동사/형용사 어미 패턴 확장
    private static final Pattern NON_NOUN_PATTERN = Pattern.compile(".*(다|하다|되다|있다|없다|시다|려고|면서|으며|지만|거나|든지|라도|부터|까지|에서|으로|이다|아니다|이야|이네|이에요|어요|아요|습니다|ㅂ니다|하네|하지|하더|하면|하고|해서|했다|할까|했어|했네|했지|했더|했으|했는|했던|한다|한데|한테|한테서|하려|하자|하기|한번|할때|할수|한것|한거|한지|했을|했던|할지|할것|하는중|하고있|해보|해야|해도|했지만|했으면|했었다|했었어|했었지|했었더|에요|어서|어도|니다|입니다|합니다|길어서|좋아서|깔끔하게|잘)$");


    /**
     * 문장 부호 기준으로 텍스트를 문장 리스트로 분리
     */
    private List<String> splitSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 마침표, 물음표, 느낌표 및 줄바꿈을 기준으로 문장 분리
        return Arrays.stream(text.split("[.!?\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * [NEW] 명사 정규화 (조사 제거)
     * 예: "말차라떼도" -> "말차라떼", "파이나" -> "파이"
     */
    private String normalizeNoun(String noun) {
        if (noun == null || noun.length() <= 1) {
            return noun;
        }
        return noun.replaceAll("(도|나|는|은|이|가|을|를)$", "");
    }
    
    /**
     * [NEW] 명사 여부 판별 메서드
     * 동사/형용사/부사 등을 필터링하고 명사만 추출
     */
    private boolean isNoun(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        String normalizedWord = normalizeNoun(word.trim());
        
        // 1. 기본 패턴 체크: 한글 2글자 이상
        if (!NOUN_PATTERN.matcher(normalizedWord).matches()) {
            return false;
        }
        
        // 2. 동사/형용사 어미 패턴 제외
        if (NON_NOUN_PATTERN.matcher(normalizedWord).matches()) {
            return false;
        }
        
        // 3. 불용어 제외
        if (STOP_WORDS.contains(normalizedWord)) {
            return false;
        }
        
        // 4. 추가 필터링: 단순 반복 문자 제외
        if (normalizedWord.matches("(.)\\1+")) { // 예: "ㅋㅋㅋ", "ㅎㅎㅎ"
            return false;
        }
        
        return true;
    }

    /**
     * 키워드 순위 분석 (명사 위주 추출, 핵심/일반 분리 및 정규화/불용어 처리 강화)
     */
    public Map<String, Object> extractKeywordRankings(String text) {
        List<String> tokens = tokenizeSimple(text);

        Map<String, Object> result = new HashMap<>();

        if (tokens.isEmpty()) {
            result.put("keyTermsCount", new HashMap<>());
            result.put("generalKeywordsRank", new ArrayList<>());
            return result;
        }

        // [MODIFIED] 명사 위주 필터링 및 정규화 강화
        Map<String, Long> allFrequency = tokens.stream()
                .filter(this::isNoun) // 명사 여부 판별 추가
                .map(this::normalizeNoun) // 명사 정규화 적용
                .filter(noun -> noun.length() > 1) // 1글자 제외
                .collect(Collectors.groupingBy(noun -> noun, Collectors.counting()));

        Map<String, Long> keyTermsCount = new HashMap<>();
        Map<String, Long> generalKeywordsCount = new HashMap<>();

        allFrequency.forEach((word, count) -> {
            if (KEY_TERMS.contains(word)) {
                keyTermsCount.put(word, count);
            } else {
                generalKeywordsCount.put(word, count);
            }
        });

        // 키워드를 최소 10개, 최대 20개 분석
        List<Map.Entry<String, Long>> generalRankings = generalKeywordsCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20) // 먼저 최대 20개까지 가져옴
                .collect(Collectors.toList());
                
        // 만약 10개 미만이라면 빈도가 낮더라도 10개까지 채우기
        if (generalRankings.size() < 10) {
            // 모든 토큰에서 추가 키워드 찾기 (명사 필터링 완화)
            Map<String, Long> additionalKeywords = tokens.stream()
                .map(this::normalizeNoun)
                .filter(word -> word.length() > 1)
                .filter(word -> !KEY_TERMS.contains(word)) // 핵심 키워드 제외
                .filter(word -> !generalKeywordsCount.containsKey(word)) // 이미 포함된 것 제외
                .filter(word -> !STOP_WORDS.contains(word)) // 불용어 제외
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));
                
            List<Map.Entry<String, Long>> additionalRankings = additionalKeywords.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10 - generalRankings.size()) // 10개까지 채우기
                .collect(Collectors.toList());
                
            generalRankings.addAll(additionalRankings);
        }

        result.put("keyTermsCount", keyTermsCount);
        result.put("generalKeywordsRank", generalRankings);

        return result;
    }


    /**
     * [NEW] 고급 키워드별 감성 분석 (사전 기반 + 컨텍스트 규칙)
     */
    public Map<String, Map<String, Integer>> analyzeSentimentByKeyTerms(String text) {
        Map<String, Map<String, Integer>> sentimentByKeyword = new HashMap<>();
        KEY_TERMS.forEach(term -> {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("POSITIVE", 0);
            counts.put("NEGATIVE", 0);
            sentimentByKeyword.put(term, counts);
        });

        List<String> sentences = splitSentences(text);

        for (String sentence : sentences) {
            List<String> tokens = tokenizeSimple(sentence);
            
            // 문장에서 발견된 키워드들
            Set<String> foundKeywords = new HashSet<>();
            for (String token : tokens) {
                String normalized = normalizeNoun(token);
                if (KEY_TERMS.contains(normalized)) {
                    foundKeywords.add(normalized);
                }
            }
            
            if (foundKeywords.isEmpty()) {
                continue; // 키워드가 없는 문장은 스킵
            }
            
            // 각 키워드에 대한 감성 분석
            for (String keyword : foundKeywords) {
                String sentiment = analyzeKeywordSentimentInSentence(sentence, keyword, tokens);
                
                if ("POSITIVE".equals(sentiment)) {
                    sentimentByKeyword.get(keyword).put("POSITIVE", 
                        sentimentByKeyword.get(keyword).get("POSITIVE") + 1);
                } else if ("NEGATIVE".equals(sentiment)) {
                    sentimentByKeyword.get(keyword).put("NEGATIVE", 
                        sentimentByKeyword.get(keyword).get("NEGATIVE") + 1);
                }
            }
        }
        
        // 1:1 비율시 positive bias 적용
        return applyPositiveBias(sentimentByKeyword);
    }
    
    /**
     * [NEW] 문장 내에서 특정 키워드의 감성 분석 (사전 기반 + 컨텍스트)
     */
    private String analyzeKeywordSentimentInSentence(String sentence, String keyword, List<String> tokens) {
        Set<String> positiveWords = KEYWORD_POSITIVE_WORDS.getOrDefault(keyword, Set.of());
        Set<String> negativeWords = KEYWORD_NEGATIVE_WORDS.getOrDefault(keyword, Set.of());
        
        boolean hasPositive = false;
        boolean hasNegative = false;
        boolean hasNegativeContext = false;
        
        // 토큰들을 순회하며 감성어 및 부정 맥락어 검사
        for (String token : tokens) {
            String normalized = normalizeNoun(token);
            
            // 긍정어 검사
            for (String posWord : positiveWords) {
                if (token.contains(posWord) || normalized.contains(posWord)) {
                    hasPositive = true;
                    break;
                }
            }
            
            // 부정어 검사
            for (String negWord : negativeWords) {
                if (token.contains(negWord) || normalized.contains(negWord)) {
                    hasNegative = true;
                    break;
                }
            }
            
            // 부정 맥락어 검사
            for (String contextWord : NEGATIVE_CONTEXT_WORDS) {
                if (token.contains(contextWord) || normalized.contains(contextWord)) {
                    hasNegativeContext = true;
                    break;
                }
            }
        }
        
        // 감성 판단 로직
        if (hasNegative) {
            return "NEGATIVE";
        } else if (hasPositive && hasNegativeContext) {
            // 긍정어 + 부정 맥락어 → 부정 (예: "맛있긴 하지만 아쉬웠어요")
            return "NEGATIVE";
        } else if (hasPositive) {
            return "POSITIVE";
        }
        
        // 사전에 없는 경우 기존 전체 감성분석 로직 사용
        Map<String, Object> sentimentResult = analyzeSentiment(sentence);
        String overallSentiment = (String) sentimentResult.get("sentiment");
        
        if ("NEUTRAL".equals(overallSentiment)) {
            return null; // 중립은 집계하지 않음
        }
        
        return overallSentiment;
    }
    
    /**
     * [NEW] 1:1 비율시 positive bias 적용
     */
    private Map<String, Map<String, Integer>> applyPositiveBias(Map<String, Map<String, Integer>> sentimentByKeyword) {
        for (Map.Entry<String, Map<String, Integer>> entry : sentimentByKeyword.entrySet()) {
            Map<String, Integer> counts = entry.getValue();
            int positive = counts.get("POSITIVE");
            int negative = counts.get("NEGATIVE");
            
            // 1:1 비율인 경우 positive bias 적용 (negative를 1 감소)
            if (positive == negative && positive > 0) {
                counts.put("NEGATIVE", negative - 1);
                counts.put("POSITIVE", positive); // positive는 그대로 유지
            }
        }
        return sentimentByKeyword;
    }

    /**
     * 주요 키워드가 포함된 문장만을 대상으로 한 이진 감성분석 (POSITIVE/NEGATIVE)
     * - 키워드 포함 문장들의 감성만 합산하여 최종 라벨 결정
     * - 키워드 포함 문장이 없으면 label=UNKNOWN 으로 반환 (호출 측에서 백업 로직 사용)
     * 반환 예시: { label: "POSITIVE"|"NEGATIVE"|"UNKNOWN", posCount: n, negCount: n, matchedSentences: m }
     */
    public Map<String, Object> classifyBinaryByKeyTerms(String text) {
        Map<String, Object> result = new HashMap<>();
        List<String> sentences = splitSentences(text);

        int positiveSentenceCount = 0;
        int negativeSentenceCount = 0;
        int matchedSentences = 0;

        for (String sentence : sentences) {
            // 문장에 주요 키워드가 포함되어 있는지 검사
            boolean containsKeyTerm = false;
            for (String token : tokenizeSimple(sentence)) {
                String normalized = normalizeNoun(token);
                if (KEY_TERMS.contains(normalized)) {
                    containsKeyTerm = true;
                    break;
                }
            }

            if (!containsKeyTerm) {
                continue;
            }

            Map<String, Object> sentimentResult = analyzeSentiment(sentence);
            String sentenceSentiment = (String) sentimentResult.get("sentiment");

            if ("POSITIVE".equals(sentenceSentiment)) {
                positiveSentenceCount++;
                matchedSentences++;
            } else if ("NEGATIVE".equals(sentenceSentiment)) {
                negativeSentenceCount++;
                matchedSentences++;
            } else {
                // NEUTRAL 은 합산에서 제외
            }
        }

        String label = "UNKNOWN";
        if (matchedSentences > 0) {
            label = positiveSentenceCount >= negativeSentenceCount ? "POSITIVE" : "NEGATIVE";
        }

        result.put("label", label);
        result.put("posCount", positiveSentenceCount);
        result.put("negCount", negativeSentenceCount);
        result.put("matchedSentences", matchedSentences);
        return result;
    }


    // ===================================================================
    // 아래는 기존 메소드들입니다 (수정 없음, analyzeSentiment만 참고용으로 사용됨)
    // ===================================================================

    public List<String> tokenizeSimple(String text) {
        if (text == null || text.trim().isEmpty()) { return new ArrayList<>(); }
        List<String> tokens = new ArrayList<>();
        String cleanText = text.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");
        for (String word : cleanText.split("\\s+")) {
            if (!word.trim().isEmpty()) {
                tokens.add(word.trim());
            }
        }
        return tokens;
    }
    
    public Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> result = new HashMap<>();
        Set<String> positiveWords = Set.of("좋", "훌륭", "최고", "만족", "추천", "괜찮", "맛있", "친절", "깨끗", "빠르", "편리", "감사", "완벽", "멋지", "사랑", "기쁘", "행복", "즐거", "재미", "신선", "맛", "품질", "서비스");
        Set<String> negativeWords = Set.of("나쁘", "최악", "불만", "별로", "싫", "맛없", "실망", "짜증", "불친절", "더럽", "느리", "불편", "화나", "문제", "오류", "고장", "비싸", "늦", "틀리", "잘못", "부족", "어려", "복잡", "답답");
        List<String> tokens = tokenizeSimple(text);
        int positiveScore = 0;
        int negativeScore = 0;
        for (String token : tokens) {
            for (String pos : positiveWords) { if (token.contains(pos)) { positiveScore++; break; }}
            for (String neg : negativeWords) { if (token.contains(neg)) { negativeScore++; break; }}
        }
        if (positiveScore > negativeScore) { result.put("sentiment", "POSITIVE");
        } else if (negativeScore > positiveScore) { result.put("sentiment", "NEGATIVE");
        } else { result.put("sentiment", "NEUTRAL"); }
        return result;
    }
}