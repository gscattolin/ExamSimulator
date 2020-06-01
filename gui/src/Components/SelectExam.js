import React,{Component} from "react";
import { Redirect,Route } from 'react-router-dom'
import Question from "./Question";

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



    componentDidMount()
        {
            fetch('http://localhost:9000/exam')
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
                <div className="row">
                    <div className="col"/>
                    <div className="col">
                        <form  onSubmit={this.handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="ExamAv">Exams Available</label>
                            </div>
                            <div className="form-group">
                                <div className="list-group">
                                    {exams.map(ex => (
                                        <button type="button" title={ex.code} key={ex.id} name={ex.id}  onClick={this.handleSelect}
                                                className="list-group-item d-flex justify-content-between align-items-center">
                                            <span className="badge badge-primary badge-pill">Questions={ex.questions}</span>
                                        </button>
                                    ))}
                                    <input id="totalNumberQ" name="totalNumberQ" type="number" defaultValue="100" onChange={this.handleSelect} />
                                    <button type="submit" className="btn btn-primary">Create Assessment</button>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div className="col"/>
                    <div className="col"/>
                    <div className="col"/>
                </div>
            )
        }
    }
}

export default SelectExam