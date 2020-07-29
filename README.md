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

## Installation

- Install Docker
- 
- Install sbt
- in sbt shell ->  to import data files of the exams inside mongo

### Clone

- Clone this repo to your local machine using `https://github.com/fvcproductions/SOMEREPO`

### Setup

- If you want more syntax highlighting, format your code like this:

> Run docker-compose.yml 


> now run sbt shell

```shell
runMain examData.initMongoDb "app/ExamData/"
```

- For all the possible languages that support syntax highlithing on GitHub (which is basically all of them), refer <a href="https://github.com/github/linguist/blob/master/lib/linguist/languages.yml" target="_blank">here</a>.

---

## Features

- Create an assessment with a custom number of question based on a pool
- Timer which shows time ypu have spent on it wit pause option.
- Final report which include statistics and all the questions with the correct answers.
- Importing new exam based on a json file
- Saving/Loading an assessment for reviewing


## Usage (Optional)

## Documentation (Optional)

The backend is in Scala 2.13.2/ Play Framework with REST API approach. 
The frontend is reactJS , located gui folder. 

## Roadmap
- Improve CSS
- Support graphical questions/answers
- Improving external files support

## FAQ

- **How do I do *specifically* so and so?**
    - No problem! Just do this.

---

## Support

Reach out to me at one of the following places!


---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](http://opensource.org/licenses/mit-license.php)**
