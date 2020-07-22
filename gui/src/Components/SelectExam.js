import React,{Component} from "react";
import { Redirect } from 'react-router-dom'
import {Tab,Row,Col,ListGroup,Toast,ToastBody,ToastHeader} from 'react-bootstrap';
import config from './config'

class SelectExam extends Component{
    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleAssessmentList = this.handleAssessmentList.bind(this);
        this.loadAssessment = this.loadAssessment.bind(this);
        this.TabExamsId = React.createRef();
        this.questionsNumber = React.createRef();
        this.state = {
            isLoaded: false,
            questionId:1,
            selectedExamId: 0,
            exams: [],
            assessments : [],
            selectedAssessmentId: 0,
            redirectToReferrer: false,
        }
    }


    fetchExams(){
        fetch(config.baseUrl+'exam')
            .then(res => res.json()).then(data => {
                this.setState({
                    error:null,
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

    fetchAssessments(){
        fetch(config.baseUrl+'assessment')
            .then(res => res.json()).then(data => {
                this.setState({
                    error:null,
                    isLoaded: true,
                    questions:0,
                    assessments:data})
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

    loadAssessment(){
        const  url=config.baseUrl+'assessment/'+this.state.selectedAssessmentId
        fetch(url,{
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                    'Content-Type': 'application/json'
            }
        })
            .then(res => res.json()).then(data => {
                this.setState({
                    error:null,
                    isLoaded: true,
                    questionId:data['questionId'],
                    redirectToReferrer : true
                })
                console.log("Get last question="+data['questionId'])
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

    componentDidMount() {
        this.fetchExams()
        this.fetchAssessments()
        }


    handleAssessmentList(event){
        if (event.target){
            event.target.className="list-group-item d-flex justify-content-between align-items-center active"
            this.setState({
                selectedAssessmentId:event.target.id
            })
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        let selectedExamId="0"
        debugger
        if (this.TabExamsId.current){
            const idV=this.TabExamsId.current.id
            if (idV.split("#").length>1){
                const v=idV.split("#")[1]
                selectedExamId=v
                this.setState({selectedExamId:v})}
        }
        let totalExams=100
        if (this.questionsNumber.current){
            totalExams=parseInt(this.questionsNumber.current.value)
        }
        fetch('/assessment', {
            method: 'POST',
            body: JSON.stringify({"examId": selectedExamId, "questions": totalExams}),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        }).then(res => res.json()).then(data => {
            this.setState({
                    selectedAssessmentId: data.assessmentId,
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
        const { error, isLoaded,exams,assessments } = this.state;
        let disableCreate=true
        let disableAssessmentButtons=true
        if (this.state.redirectToReferrer === true) {
            const url="/Question/"+this.state.selectedAssessmentId+"/"+this.state.questionId
            return <Redirect to={url} />
        }
        if (this.TabExamsId.current){
            disableCreate= this.TabExamsId.current.id.indexOf("#")===-1
        }
        if(this.state.selectedAssessmentId){
            disableAssessmentButtons=this.state.selectedAssessmentId===0
        }
        if (error) {
            return <div>Error: {error.message}</div>;
        } else if (!isLoaded) {
            return <div>Loading...</div>;
        } else {
            return(
                <div className="container-fluid">
                    <div className="row my-lg-2">
                        <div className="col-6">
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Exams List Available</h5>
                                    <Tab.Container id="list-group-tabs-exams" defaultActiveKey="#1">
                                        <Row>
                                            <Col sm={4}>
                                                <ListGroup>
                                                    {exams.map(ex => (
                                                            <ListGroup.Item key={ex.id} action href={"#"+ex.id} >
                                                                {ex.code}
                                                            </ListGroup.Item>
                                                        ))}
                                                </ListGroup>
                                            </Col>
                                            <Col sm={8}>
                                                <Tab.Content>
                                                    {exams.map(ex => (
                                                        <Tab.Pane eventKey={"#"+ex.id} key={ex.id} ref={this.TabExamsId} >
                                                            <b>{ex.title}</b><br/>Version={ex.version}<br/>Total Pool Questions={ex.questions}
                                                        </Tab.Pane>
                                                    ))}
                                                </Tab.Content>
                                            </Col>
                                        </Row>
                                    </Tab.Container>
                                </div>
                            </div>
                        </div>
                        <div className="col-6">
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Assessments Available</h5>
                                    <div className="row">
                                        <div className="col-4">
                                            <div className="list-group" >
                                                {assessments.map(a =>
                                                    <button type="button" className="list-group-item d-flex justify-content-between align-items-center" id={a.Id} key={a.Id} onClick={this.handleAssessmentList}>
                                                        {a.Code}
                                                        <span className="badge badge-primary badge-pill">Q={a.QuestionsNumber}</span>
                                                    </button>
                                                )}
                                            </div>
                                            <div className="col-8"/>
                                        </div>
                                    </div>
                                    <div className="row mt-lg-1">
                                        <div className="col-4">
                                            <div className="btn-group px-md-t10" role="group">
                                                <button type="button" className="btn btn-success" disabled={disableAssessmentButtons} onClick={this.loadAssessment} >Load&Start</button>
                                                <button type="button" className="btn btn-danger" disabled={disableAssessmentButtons} >Delete</button>
                                            </div>
                                        </div>
                                        <div className="col-8"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="row my-lg-2"/>
                    <div className="row my-lg-2">
                        <div className="col-2">
                            <button type="submit" disabled={disableCreate} className="btn btn-primary" onClick={this.handleSubmit}>Create Assessment</button>
                        </div>
                        <div className="col-4">
                            Questions in Assessment:
                            <input id="totalNumberQ" name="totalNumberQ" type="number" defaultValue="100" ref={this.questionsNumber}/>
                        </div>
                    </div>
                    <div className="col-6"/>
                </div>
            )
        }
    }
}

export default SelectExam