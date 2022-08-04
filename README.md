# Oracle-ADF-and-JSF-Utils-libraries
ADFUtils and JSFUtils are two libraries of must use functions to use in Oracle ADF applications.

Original library authors are : Duncan Mills, Steve Muench and Ric Smith.
They were last updated in 2007.

Since I've been working on clients Oracle ADF projects for the last 10 years, and the first mandatory steps are always the same : 

– Download the latest version of the Oracle ADF Demo project (FusionOrderDemo) 

– Search and copy the ADFUtils and JSFUtils java class

– Add them to your ViewController projet

 
Those libraries are so important, they should be added by Oracle to the core oracle ADF libraries.
Since they aren't, and we still have to manually add them to our project, I decided to set up this public repository to have them easily accessible to everyone. 
And so, without having to search the web for the Oracle ADF Demo project. 
Please remember, they were firstly open source by their original authors.

I've also added my own functions to the libraries over the years. 

Hope they'll be as useful to you as they've been to me and my clients.

Looking for example on how to use those in your own Oracle ADF projects, find them here : https://cedricleruth.com/category/tutorials/oracle/oracleadf/

To use it in your ADF project : 

 1) In your ViewController projet, create 2 java class in a package "view.utils":
 
     a) Name one of the java class ADFUtils.java
     
     b) Name the second java class JSFUtils.java
     
 2) Copy the content of this JSFUtils into your newly created JSFUtils.java
 
 3) Copy the content of this ADFUtils into your newly created ADFUtils.java
 
 4) Add an "import view.utils.JSFUtils;" at the top of your ADFUtils.java file (Is often done automatically by JDeveloper)
 
 5) You can now use those functions in all your java beans in your ViewController project
    
