package com.reviewgenie.service;

import java.util.List;
import java.util.Map;

/**
 * 한국어 NLP 서비스 인터페이스
 * 한국어 리뷰 분석을 위한 다양한 기능들을 제공합니다.
 */
public interface KoreanNLPServiceInterface {

    /**
     * 키워드 순위 분석 (명사 위주 추출, 핵심/일반 분리 및 정규화/불용어 처리)
     * 
     * @param text 분석할 텍스트
     * @return 키워드 순위 분석 결과
     *         - keyTermsCount: 핵심 키워드별 빈도
     *         - generalKeywordsRank: 일반 키워드 순위 (빈도순)
     */
    Map<String, Object> extractKeywordRankings(String text);

    /**
     * 고급 키워드별 감성 분석 (사전 기반 + 컨텍스트 규칙)
     * 
     * @param text 분석할 텍스트
     * @return 키워드별 감성 분석 결과
     *         - 각 키워드별 POSITIVE/NEGATIVE 카운트
     */
    Map<String, Map<String, Integer>> analyzeSentimentByKeyTerms(String text);

    /**
     * 주요 키워드가 포함된 문장만을 대상으로 한 이진 감성분석
     * 
     * @param text 분석할 텍스트
     * @return 이진 감성분석 결과
     *         - label: "POSITIVE"|"NEGATIVE"|"UNKNOWN"
     *         - posCount: 긍정 문장 수
     *         - negCount: 부정 문장 수
     *         - matchedSentences: 키워드가 포함된 문장 수
     */
    Map<String, Object> classifyBinaryByKeyTerms(String text);

    /**
     * 간단한 토큰화 (한글, 영문, 숫자만 추출)
     * 
     * @param text 토큰화할 텍스트
     * @return 토큰 리스트
     */
    List<String> tokenizeSimple(String text);

    /**
     * 기본 감성 분석 (긍정/부정/중립)
     * 
     * @param text 분석할 텍스트
     * @return 감성 분석 결과
     *         - sentiment: "POSITIVE"|"NEGATIVE"|"NEUTRAL"
     */
    Map<String, Object> analyzeSentiment(String text);

    /**
     * 문장을 문장 단위로 분리
     * 
     * @param text 분리할 텍스트
     * @return 문장 리스트
     */
    List<String> splitSentences(String text);

    /**
     * 명사 정규화 (조사 제거)
     * 
     * @param noun 정규화할 명사
     * @return 정규화된 명사
     */
    String normalizeNoun(String noun);

    /**
     * 명사 여부 판별
     * 
     * @param word 판별할 단어
     * @return 명사 여부
     */
    boolean isNoun(String word);
}
