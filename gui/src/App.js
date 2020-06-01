import React,{Component} from 'react';
import { Route } from 'react-router-dom'
import SelectExam from "./Components/SelectExam"

import Question from "./Components/Question";
import Report from "./Components/Report";


function App() {
  return (
    <div className="App">
        <div className="container-fluid">
            <div className="row">
                <div className="col"/>
                <div className="col">
                    <h1>Exam Simulator v 0.1</h1>
                </div>
                <div className="col"/>
            </div>
            <div className="row">
            </div>
        </div>
        <Route path="/Start" component={SelectExam} />
        <Route path="/Question/:assessmentId/:questionId" component={Question} />
        <Route path="/Report/:assessmentId" component={Report} />
    </div>
  );
}

export default App;
