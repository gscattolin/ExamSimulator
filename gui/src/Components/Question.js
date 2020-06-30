import React,{Component} from "react";
import {Redirect} from "react-router-dom";
import update from 'immutability-helper';
import config from './config'
import leftArrow from '../Images/icons8-previous-48.png';
import rightArrow from '../Images/icons8-next-48.png';


class Question extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClickNextForward =this.handleClickNextForward.bind(this);
        this.handleInputChange =this.handleInputChange.bind(this);
        const now=Date.now()
        const urlR="/Question/"+props.match.params.assessmentId+"/"+props.match.params.questionId
        this.state = {
            assessmentId:props.match.params.assessmentId,
            questionId:props.match.params.questionId,
            url:urlR,
            isLoaded:false,
            render: false,
            redirect:false,
            submitEnable:false,
            startDate:now,
            timer:null,
            answersUser: new Map()
        }
    }

    inputName() {
        return "inputAnswers";
    }

    componentWillUnmount() {
        clearInterval(this.timerID);
    }

    tick() {
        const diff= new Date()-this.state.startDate
        this.setState({
            timer: diff
        });
    }

    getNewQuestion(questionId){
        this.setState({isLoaded:false})
        // console.log("Getting data question"+questionId)
        const url=config.baseUrl+'assessment/'+this.state.assessmentId+'/question/'+questionId
        fetch(url)
            .then(res => res.json()).then(data => {
                this.setState({
                    error:null,
                    questionId:questionId,
                    isLoaded: true,
                    redirect:false,
                    render:true,
                    question:data})
            },
            (error) => {
                console.log("ERROR Getting data question"+error)
                this.setState({
                    isLoaded: true,
                    error:error
                });
            }
        )
    }

    getTotalQuestions(assessmentId){
        // console.log("Getting data question"+questionId)
        const url=config.baseUrl+'assessment/'+assessmentId+'/question'
        fetch(url)
            .then(res => res.json()).then(data => {
                this.setState({
                        error: null,
                        submitEnable:false,
                        totalQuestions: data.TotalQuestions,
                    }
                )
            },
            (error) => {
                console.log("ERROR Getting data question"+error)
                this.setState({
                    error:error
                });
            }
        )
    }

    componentDidMount() {
        this.timerID = setInterval(
            () => this.tick(),
            1000
        );
        this.setState({
            startDate:new Date(),
            timer:null,
        })
        this.getNewQuestion(1)
        this.getTotalQuestions(this.state.assessmentId)
    }

    componentDidUpdate(prevProps) {
        if (this.props.match.params.questionId !== prevProps.match.params.questionId) {
            // console.log("Component update with QId"+this.props.match.params.questionId)
            this.getNewQuestion(this.props.match.params.questionId)
        }
    }

    handleInputChange(event) {
        const valuesSelected = Array
            .from(document.getElementsByName(this.inputName()))
            .filter((el) => el.checked)
            .map((el) => el.value);
        const newA=update(this.state.answersUser,{[this.state.questionId]:{$set: valuesSelected}})
        this.setState({
            answersUser:newA,
            render:true,
        })
    }

    handleSubmit(event) {
        event.preventDefault();
        //console.log("Send submit ass="+this.state.assessmentId+'/question='+this.state.questionId)
        const url=config.baseUrl+'assessment/'+this.state.assessmentId
        const aws=this.state.answersUser
        const bodyT=JSON.stringify({"answers": Array.from(aws.entries())})
        fetch(url, {
            method: 'PUT',
            body: bodyT,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        }).then(res => {if (res.status>300)
        {
            console.error("Error Getting data question ="+JSON.stringify(res))
        }
        else
        {
            const url="/Report/"+this.state.assessmentId
            this.setState({
                redirect:true,
                url:url,
            })
        }
        })

    }

    preFillAnswers(question){
        let answers=question.Answers
        if (this.state.answersUser.has(this.state.questionId.toString()))
        {
            const preAnsw=this.state.answersUser.get(this.state.questionId.toString())
            for (let i = 0; i < answers.length; i++) {
                answers[i].checked=preAnsw.includes(answers[i].placeHolder)
            }
        }
        else{
            for (let i = 0; i < answers.length; i++) {
                answers[i].checked=false
            }
        }
    }

    handleClickNextForward(event){
        const delta = event.target.alt==="back" ? -1: 1
        const nextQ=parseInt(this.state.questionId)+delta
        if(nextQ<parseInt(this.state.totalQuestions)){
            const url="/Question/"+this.state.assessmentId+"/"+nextQ
            this.setState({
                questionId:nextQ,
                url:url,
                redirect:true,
            })
        }else if (nextQ===parseInt(this.state.totalQuestions))
        {
            const url="/Question/"+this.state.assessmentId+"/"+nextQ
            this.setState({
                questionId:nextQ,
                url:url,
                redirect:true,
                submitEnable:true
            })
        }

    }

    timeFromSec2Format(d){
        let h = Math.floor(d / 3600);
        let m = Math.floor(d % 3600 / 60);
        let s = Math.floor(d % 3600 % 60);
        if (h>0) return h+" h "+m+" min "+s+" s"
        return m+" min "+s+" s"

    }

    render() {
        const {error, isLoaded, question} = this.state;
        if (this.state.redirect === true) {
            return <Redirect to={this.state.url} />
        }
        if (error) {
            return <div>Error: {error.message}</div>;
        } else if (!isLoaded || !this.state.render) {
            return <div>Loading...</div>;
        } else {
            // console.log("rendering question state url="+JSON.stringify(this.state))
            this.preFillAnswers(question)
            const isOneAnswer=question.CorrectAnswers.length===1 ? "radio" : "checkbox"
            const leftEnable=this.state.questionId!==1
            const rightEnable=this.state.answersUser.has(this.state.questionId.toString()) ? this.state.answersUser.get(this.state.questionId.toString()).length>0 : false
            var timePassed=0
            if (this.state.timer){
                timePassed=this.timeFromSec2Format(this.state.timer/1000)
            }
            return (
                <div className="form-group" >
                    <div className="container-fluid ">
                         <div className="row my-lg-2">
                        <div className="col"/>
                        <div className="col">
                            <h2>Question N. {this.state.question.Id}/{this.state.totalQuestions}</h2>
                        </div>
                            <div className="col">
                                {/*Time= {this.state.timer.getHours()} H {this.state.timer.getMinutes()} min*/}
                                {/*{this.state.timer.getSeconds()} s*/}
                                Timer={timePassed}
                            </div>
                    </div>
                        <div className="row ml-lg-0">
                            <div className="col m-3">
                                <label>{this.state.question.Text} </label>
                            </div>
                        </div>
                        <div className="row ml-lg-0">
                            <div className="col m-3">
                                    {question.Answers.map(aws => (
                                        <div key={aws.placeHolder} className="form-check my-3">
                                        <input  className="form-check-input " name={this.inputName()} type={isOneAnswer} onChange={this.handleInputChange}
                                               value={aws.placeHolder} id={aws.placeHolder} checked={aws.checked}/>
                                            <label className="form-check-label " htmlFor={aws.placeHolder}>
                                                {aws.Text}
                                            </label>
                                        </div>
                                    ))}
                            </div>
                        </div>
                        <div className="row ml-lg-0">
                            <div className="col m-2">
                                <button onClick={this.handleClickNextForward} disabled={!leftEnable}>
                                    <img className="img-back" src={leftArrow} alt="back"/>
                                </button>
                                <button disabled={!rightEnable} onClick={this.handleClickNextForward}>
                                    <img className="img-forward" src={rightArrow} alt="forward"/>
                                </button>
                            </div>
                            <div className="col"/>
                        </div>
                        <div className="row ml-lg-10">
                            <div className="col"/>
                            <div className="col m-5">
                                <div className={this.state.submitEnable?"visible":"invisible"}>
                                    <button className="btn btn-outline-primary" onClick={this.handleSubmit} disabled={!rightEnable}>Submit Answers</button>
                                </div>
                            </div>
                            <div className="col"/>
                        </div>
                    </div>

                </div>
            )
        }
    }
}

export default Question