# Oracle ADFUtils and JSFUtils libraries
ADFUtils and JSFUtils are two libraries of must use functions to use in Oracle ADF applications.

Original library authors are : Duncan Mills, Steve Muench and Ric Smith in the Oracle Demo App.
Their versions were last updated in 2007.
 
Those libraries are a must use on all Oracle ADF projects.
Since they aren't part of the framework core, we still need to manually add them to our project. 
Those librairies were firstly made available by their original author in the Oracle ADF Demo project.
This repository is to make them easily accessible to everyone. 

I've also added my own functions to the libraries over the years.
Hope they'll be as useful to you as they've been to me and my clients.

Looking for example on how to use those in your own Oracle ADF projects, find them here : https://cedricleruth.com/category/tutorials/oracle/oracleadf/

### To use it in your ADF project : 

 1) In your ViewController projet, create 2 java class in a package "view.utils":
 
     a) Name one of the java class ADFUtils.java
     
     b) Name the second java class JSFUtils.java
     
 2) Copy the content of this JSFUtils into your newly created JSFUtils.java
 
 3) Copy the content of this ADFUtils into your newly created ADFUtils.java
 
 4) Add an "import view.utils.JSFUtils;" at the top of your ADFUtils.java file (Is often done automatically by JDeveloper)
 
 5) You can now use those functions in all your java beans in your ViewController project
    
