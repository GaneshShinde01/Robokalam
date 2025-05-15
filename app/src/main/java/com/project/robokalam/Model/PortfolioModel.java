package com.project.robokalam.Model;

public class PortfolioModel {

    private String email;
    private String name;
    private String college;
    private String skills;
    private String projectTitle;
    private String projectDescription;

    public PortfolioModel(String email, String name, String college, String skills, String projectTitle, String projectDescription) {
        this.email = email;
        this.name = name;
        this.college = college;
        this.skills = skills;
        this.projectTitle = projectTitle;
        this.projectDescription = projectDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }
}
