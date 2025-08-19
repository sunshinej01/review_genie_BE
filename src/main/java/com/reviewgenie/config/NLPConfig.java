package com.reviewgenie.config;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

@Configuration
public class NLPConfig {

    @Bean
    @Primary
    public StanfordCoreNLP stanfordCoreNLP() {
        // 기본 영어 모델을 사용한 텍스트 분석 설정
        Properties props = new Properties();
        // sentiment 분석을 위해 parse 추가
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        
        return new StanfordCoreNLP(props);
    }
}