import React,{Component} from "react";
import { Redirect } from 'react-router-dom'
import axios from 'axios'
import {Tab,Row,Col,ListGroup,Toast} from 'react-bootstrap'

class SelectExam extends Component{
    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleAssessmentList = this.handleAssessmentList.bind(this);
        this.handleImportFile= this.handleImportFile.bind(this)
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
            version: "0.9.9"
        }
    }

    handleError(error){
        return {
            showToastImport:true,
            toastImportData: {"code":error['code'] ? "Code "+error['code']:"","message":"Error "+error['message'] ? error['message']:""},
            isLoaded: true,
        }
    }

    handleImportFile(e){
        const  url='/exam'
        axios.put(url,e.target.files[0],{headers:{'Accept':'application/json','Content-Type':'application/json'}})
        .then(data => {
                    this.setState({
                        showToastImport:true,
                        toastImportData: {"code":data.code ? "Code "+data.code:"","message":data.questions ? "Total Question imported "+data.questions:""}
                    })
                    console.log("file imported successfully="+data['message'])
                    }
            )
        .catch( (error)=> {
            console.log("ERROR Intercepted = " + error)
            this.setState(this.handleError({"code":error.response.data.Id,"message":error.response.data.Message}));
            }
        )
    }

    fetchExams(){
        axios.get('/exam')
            .then((res) =>
                this.setState({
                    error:null,
                    questions:0,
                    exams:res.data})
        ).catch(
            (error) => {
                console.log("ERROR"+error.message)
                this.setState(this.handleError(error))
            }
        )
    }

    fetchAssessments(){
        axios.get('/assessment')
            .then((data) => {
                this.setState({
                    error:null,
                    isLoaded: true,
                    questions:0,
                    assessments:data.data})})
            .catch((error) => {
                console.log("ERROR"+error.message)
                this.setState(this.handleError(error))
            }
        )
    }

    fetchVersion(){
        axios.get('/version')
            .then(res => {
                console.log("Getting version "+res.version)
                this.setState({
                    error:null,
                    version:res.version
                })
            }).catch(
            (error) => {
                console.log("ERROR fetchVersion = "+error)
                this.setState(this.handleError(error))
            }
        )
    }

    loadAssessment(){
        const  url='/assessment/'+this.state.selectedAssessmentId
        axios.get(url,{headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}})
            .then(data => {
                this.setState({
                    error:null,
                    isLoaded: true,
                    questionId:data.data['questionId'],
                    redirectToReferrer : true
                })
                this.fetchExams()
                console.log("Get last question="+data.data['questionId'])
            }).catch(
            (error) => {
                console.log("ERROR"+error.message)
                this.setState(this.handleError(error));
            }
        )
    }

    componentDidMount() {
        this.fetchVersion()
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
        axios.post('/assessment', {"examId": selectedExamId, "questions": totalExams},{headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        }).then(res => {
            this.setState({
                    selectedAssessmentId: res.data.assessmentId,
                    redirectToReferrer : true
                })})
        .catch(
            (error) => {
                console.log("ERROR" + error.message)
                this.setState(this.handleError(error));
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
                                    <div className="col">
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
                                            <Row>
                                                <div className="input-group mb-2">
                                                    <div className="input-group-prepend">
                                                        <button type="submit" disabled={disableCreate} className="btn btn-primary" onClick={this.handleSubmit}>Create Assessment</button>
                                                    </div>
                                                    <input className="w-20" id="totalNumberQ" name="totalNumberQ" type="number" defaultValue="100" ref={this.questionsNumber}/>
                                                </div>
                                            </Row>
                                            <Row >
                                                <div className="input-group mb-2">
                                                    <div className="custom-file">
                                                        <input type="file" className="custom-file-input"
                                                               id="file2Upload" accept=".json" onChange={this.handleImportFile}/>
                                                            <label className="custom-file-label"
                                                                   htmlFor="file2Upload"
                                                                   aria-describedby="inputGroupFileAddon02">Choose
                                                                file  to Import</label>
                                                    </div>
                                                </div>
                                            </Row>
                                        </Tab.Container>
                                    </div>
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
                    <Toast onClose={() => {window.location.reload()}} show={!!this.state.showToastImport} delay={12000} autohide={true} animation={true}
                           style={{
                               position: 'relative',
                               top: 100,
                               left: 100,
                           }}>
                        <Toast.Header>
                                <strong className="mr-auto text-light bg-danger">File Imported</strong>
                                {/*<small className="mr-auto text-light bg-danger">question imported ={this.state.toastImportData ? this.state.toastImportData.questions : ""}</small>*/}
                        </Toast.Header>
                        <Toast.Body>
                        <br/>
                            {this.state.toastImportData ? (this.state.toastImportData.code ? this.state.toastImportData.code:""):""}
                        <br/>
                            {this.state.toastImportData ? (this.state.toastImportData.message ? this.state.toastImportData.message:""):""}
                        </Toast.Body>
                    </Toast>
                </div>
            )
        }
    }
}

export default SelectExam