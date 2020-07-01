import React,{Component} from "react";
import { Redirect,Route } from 'react-router-dom'
import {Tab,Row,Col,ListGroup} from 'react-bootstrap';
import config from './config'

class SelectExam extends Component{
    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleSelect= this.handleSelect.bind(this);
        this.state = {
            isLoaded: false,
            selectexamId: 0,
            exams: [],
            redirectToReferrer: false,
        }
    }


    componentDidMount() {
            fetch(config.baseUrl+'exam')
                .then(res => res.json()).then(data => {
                            this.setState({
                                error:null,
                                isLoaded: true,
                                selectexamId: '',
                                questions:0,
                                exams:data})
                        },
                (error) => {
                    console.log("ERROR"+error.message)
                    this.setState({
                        isLoaded: true,
                        error
                    });
                }
                    )
        }

    handleSelect(e){
        if (e.target.name==="totalNumberQ"){
            this.state.questions=e.target.value
        }
        if (e.target.type==="button"){
            this.state.selectexamId=e.target.name
            console.log("sel="+this.state.selectexamId)
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        const form = event.target;
        const totalExams = form.elements["totalNumberQ"].value
        console.log("-----sel this.state.selectexamId.length<1 ----"+this.state.selectexamId.length<1)
        if (this.state.selectexamId.length<1) this.state.selectexamId="1"
        fetch('/assessment', {
            method: 'POST',
            body: JSON.stringify({"examId": this.state.selectexamId, "questions": totalExams}),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        }).then(res => res.json()).then(data => {
            this.setState({
                    assessmentId: data.assessmentId,
                    redirectToReferrer : true
                })},
                (error) => {
                    console.log("ERROR" + error.message)
                    this.setState({
                        isLoaded: true,
                        error
                    });
                }
            )
    }


    render(){
        const { error, isLoaded, selectexamId,questions,exams } = this.state;
        if (this.state.redirectToReferrer === true) {
            // return <Redirect to={{pathname: '/Question',
            //     state:{ "assessmentId":this.state.assessmentId,"questionId":1}}}/>
            // return <Route path="/Question/:assessmentId/1" exact={true} component={Question} />
            const url="/Question/"+this.state.assessmentId+"/1"
            return <Redirect to={url} />
        }
        if (error) {
            return <div>Error: {error.message}</div>;
        } else if (!isLoaded) {
            return <div>Loading...</div>;
        } else {
            return(
                <div className="container-fluid">
                    <div className="row my-lg-2">
                        <div className="col-8">
                            <Tab.Container id="list-group-tabs-exams" defaultActiveKey="#1">
                                <Row>
                                    <Col sm={4}>
                                        <ListGroup>
                                            {exams.map(ex => (
                                                    <ListGroup.Item key={ex.id} action href={"#"+ex.id} >
                                                        {ex.code}---
                                                        Questions={ex.questions}
                                                    </ListGroup.Item>
                                                ))}
                                        </ListGroup>
                                    </Col>
                                    <Col sm={8}>
                                        <Tab.Content>
                                            {exams.map(ex => (
                                                <Tab.Pane eventKey={"#"+ex.id} key={ex.id}>
                                                    {ex.title}-{ex.version}
                                                </Tab.Pane>
                                            ))}
                                        </Tab.Content>
                                    </Col>
                                </Row>
                            </Tab.Container>
                        </div>
                        <div className="col-4"/>
                    </div>
                    <div className="row my-lg-2"/>
                    <div className="row my-lg-2">
                        <div className="col-2">
                            <button type="submit" className="btn btn-primary">Create Assessment</button>

                        </div>
                        <div className="col-4">
                            Questions in Assessment:
                            <input id="totalNumberQ" name="totalNumberQ" type="number" defaultValue="100" onChange={this.handleSelect} />
                        </div>
                    </div>
                    <div className="col-6"/>
                </div>
            )
        }
    }
}

export default SelectExam