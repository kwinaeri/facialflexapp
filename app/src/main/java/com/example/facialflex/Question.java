package com.example.facialflex;

public class Question {
    private String questionText;
    private String option1;
    private String option2;
    private String answer;  // Store answer here

    // Constructor
    public Question(String questionText, String option1, String option2) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.answer = "";  // Default answer as empty
    }

    // Getter and setter for answer
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    // Other getters and setters
    public String getQuestionText() {
        return questionText;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }
}

