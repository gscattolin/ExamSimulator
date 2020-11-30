# ExamSimulator

> This project aims to create an open source web-based Exam Simulator. 

Scala , Play , MongoDb, ReactJS

[![Build Status](https://travis-ci.org/doge/wow.svg)](https://travis-ci.org/doge/wow)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

## Table of Contents (Optional)

> If your `README` has a lot of info, section headers might be nice.

- [Installation](#installation)
- [Features](#features)
- [Roadmap](#roadmap)
- [FAQ](#faq)
- [Support](#support)
- [License](#license)

---

## Installation ans Setup

- Install [Docker](https://hub.docker.com/)  on your machine.
- Clone this [repo](https://github.com/gscattolin/ExamSimulator)
- Run all containers with `docker-compose up`
- Launch your browser and type `http://localhost:3000/start`

### Clone

- Clone this repo to your local machine using `https://github.com/gscattolin/ExamSimulator`

## Features

- Create an assessment with a custom number of question based on a pool
- Timer which shows time you have spent on it wit pause option.
- Final report which include statistics and all the questions with the correct answers.
- Importing new exam based on a json file.
```json
{
   "Title":"Amazon AWS Certified Solutions Architect - Associate Exam",
   "Code":"SAA-C01",
   "Version":"1.8",
   "TimeLimit":0,
   "Instructions":"Multiple choice, multiple answer, 130 minutes to complete",
   "Questions":[
      {
         "number":1,
         "question":"A Solutions Architect is designing an application that will encrypt all data in an Amazon Redshift cluster.Which action will encrypt the data at rest?",
         "choices":[
            {
               "placeHolder":"A.",
               "choiceValue":"Place the Redshift cluster in a private subnet."
            },
            {
               "placeHolder":"B.",
               "choiceValue":"Use the AWS KMS Default Customer master key."
            },
            {
               "placeHolder":"C.",
               "choiceValue":"Encrypt the Amazon EBS volumes."
            },
            {
               "placeHolder":"D.",
               "choiceValue":"Encrypt the data using SSL/TLS."
            }
         ],
         "answers":[
            "B"
         ],
         "reference":"Reference -https://docs.aws.amazon.com/redshift/latest/mgmt/working-with-db-encryption.html",
         "valid":true
      },
      {
         "number":2,
         "question":"A website experiences unpredictable traffic. During peak traffic times, the database is unable to keep up with the write request.Which AWS service will help decouple the web application from the database?",
         "choices":[
            {
               "placeHolder":"A.",
               "choiceValue":"Amazon SQS"
            },
            {
               "placeHolder":"B.",
               "choiceValue":"Amazon EFS"
            },
            {
               "placeHolder":"C.",
               "choiceValue":"Amazon S3"
            },
            {
               "placeHolder":"D.",
               "choiceValue":"AWS Lambda"
            }
         ],
         "answers":[
            "A"
         ],
         "reference":"Reference -https://aws.amazon.com/sqs/faqs/",
         "valid":false
      }
   ]
}
```
- Saving/Loading an assessment for reviewing

## Usage 

- Launch your browser and type `http://localhost:3000/start`
- Select a exam based on the exam code
- Type number of questions you want in your assessment
- Press create assessment button ans start to answer :)

## Documentation 

The backend is in [Scala 2.13.2](https://www.scala-lang.org/) / [Play Framework 2.8.2](https://www.playframework.com/) with REST API approach. 
The frontend is reactJS , located gui folder. 

## Roadmap
- Improve CSS
- Support graphical questions/answers
- Improving external files support

## FAQ

- **How do I import a new pool of questions?**
    - Create a json file which contains the questions (follow the sample above). Import it using the import button in the main page.

---

## Support

📫 Reach out to me at devel0pingsec0nday  at gmail dot com

---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](http://opensource.org/licenses/mit-license.php)**

[That's all folks!](https://giphy.com/gifs/qPVzemjFi150Q)

<p align="center">
  <img src="./docs/BugRickMorty.gif" width="350" title="That's all folks!">
</p>
