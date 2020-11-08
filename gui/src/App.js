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
                    <div className="media">
                        <img class="m-2" src="./logo.svg" height="32" width="32" />
                        <div className="media-body">
                            <h1>Exam Simulator </h1>
                        </div>
                    </div>
                </div>
                <div className="col">
                    <small>mode <b>{process.env.NODE_ENV}</b></small>
                </div>
            </div>
            <div className="row">
                <Route path="/Start" component={SelectExam} />
                <Route path="/Question/:assessmentId/:questionId" component={Question} />
                <Route path="/Report/:assessmentId" component={Report} />
            </div>
            <div className="row">
                <div className={this ? 'visible': 'invisible'}>
                    <div className="alert alert-danger" role="alert" >
                        <h4 className="alert-heading">Error</h4>
                        <p>Aww yeah, you successfully read this important alert message. This example text is going to run a
                            bit longer so that you can see how spacing within an alert works with this kind of content.</p>
                        <hr/>
                            <p className="mb-0">Whenever you need to, be sure to use margin utilities to keep things nice
                                and tidy.</p>
                        {this ? this.state.error : 'Nothing'}
                    </div>
                </div>
            </div>
        </div>
    </div>
  );
}

export default App;
