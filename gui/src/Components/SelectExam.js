import React,{Component} from "react";
import { Redirect,Route } from 'react-router-dom'
import {Tab,Row,Col,ListGroup,Toast,ToastBody,ToastHeader} from 'react-bootstrap';
import config from './config'

class SelectExam extends Component{
    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
        this.TabExamsId = React.createRef();
        this.questionsNumber = React.createRef();
        this.state = {
            isLoaded: false,
            selectedExamId: 0,
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
        fetch('/assessment', {
            method: 'POST',
            body: JSON.stringify({"examId": selectedExamId, "questions": totalExams}),
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
        const { error, isLoaded,exams } = this.state;
        let disableCreate=true
        if (this.state.redirectToReferrer === true) {
            // return <Redirect to={{pathname: '/Question',
            //     state:{ "assessmentId":this.state.assessmentId,"questionId":1}}}/>
            // return <Route path="/Question/:assessmentId/1" exact={true} component={Question} />
            const url="/Question/"+this.state.assessmentId+"/1"
            return <Redirect to={url} />
        }
        if (this.TabExamsId.current){
            disableCreate= this.TabExamsId.current.id.indexOf("#")===-1
        }
        console.log("Status="+disableCreate)
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
                        <div className="col-4"/>
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