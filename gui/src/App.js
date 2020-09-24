import React from 'react';
import { Route } from 'react-router-dom'
import SelectExam from "./Components/SelectExam"
import './index.css';
import 'bootstrap/dist/css/bootstrap.css';
//import './style.css';
import Question from "./Components/Question";
import Report from "./Components/Report";


function App() {
  return (
    <div className="App">
        <div className="container-fluid">
            <div className="row">
                <div className="col"/>
                <div className="col">
                    <h1>Exam Simulator </h1>

                </div>
                <div className="col">
                    <small>mode <b>{process.env.NODE_ENV}</b></small>
                </div>
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
