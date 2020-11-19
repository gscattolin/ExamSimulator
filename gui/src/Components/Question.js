import React,{Component} from "react";
import {Redirect}  from 'react-router-dom'
import axios from 'axios';
import update from 'immutability-helper';
import leftArrow from '../Images/icons8-previous-48.png';
import rightArrow from '../Images/icons8-next-48.png';
import pauseIcon from '../Images/icons8-pause-16.png';
import resumeIcon from '../Images/icons8-resume-button-16.png';
import Timer from 'react-compound-timer'
import parse from 'html-react-parser';


class Question extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClickNextForward =this.handleClickNextForward.bind(this);
        this.handleInputChange =this.handleInputChange.bind(this);
        this.onPauseResumeTime =this.onPauseResumeTime.bind(this);
        const urlR="/Question/"+props.match.params.assessmentId+"/"+props.match.params.questionId
        this.TimerRef = React.createRef();
        this.state = {
            assessmentId:props.match.params.assessmentId,
            questionId:props.match.params.questionId,
            question:{},
            url:urlR,
            isLoaded:false,
            isSaving:false,
            render: false,
            redirect:false,
            submitEnable:false,
            TimerValue: 0,
            TimerStatus:'PLAYING',
        }
    }

    inputName() {
        return "inputAnswers";
    }

    savingButtonName() {
        return "savingAssessment";
    }


    onPauseResumeTime(){
        if (this.state.TimerStatus==='PLAYING'){
            this.TimerRef.current.pause()
            this.setState({TimerStatus:'PAUSED'})
        }else{
            this.TimerRef.current.resume()
            this.setState({TimerStatus:'PLAYING'})
        }
    }

    handleError(error){
        return {
            errorId : error['errorId'],
            errorMsg: error['errorMsg'],
            isLoaded: true,
        }
    }

    getNewQuestion(questionId){
        this.setState({isLoaded:false})
        // console.log("Getting data question"+questionId)
        const url='/assessment/'+this.state.assessmentId+'/question/'+questionId
        axios.get(url)
            .then(res => {
                this.setState({
                    error:null,
                    question:res.data,
                    questionId:questionId,
                    redirect:false,
                    isLoaded: true,
                    render:true,
                    })
            // console.log("received question Id "+questionId+" data "+JSON.stringify(data))
            }).catch(
            (error) => {
                console.log("ERROR Getting data question"+error)
                this.setState(this.handleError(error));
            }
        )
    }

    getTotalQuestions(assessmentId){
        // console.log("Getting data question"+questionId)
        const url='/assessment/'+assessmentId+'/question'
        axios.get(url)
            .then(res => {
                let mapV=new Map()
                for(let n=0; n<res.data.AnswersUser.length; n++){
                    mapV.set((res.data.AnswersUser[n][0]).toString(),res.data.AnswersUser[n][1])
                }
                this.setState({
                        error: null,
                        submitEnable:false,
                        answersUser: mapV,
                        totalQuestions: res.data.TotalQuestions,
                    }
                )
            }).catch(
            (error) => {
                console.log("ERROR Getting data question"+error)
                this.setState(this.handleError(error));
            }
        )
    }

    componentDidMount() {
        this.setState({
            startDate:new Date(),
            timer:null,
        })
        this.getNewQuestion(this.state.questionId)
        this.getTotalQuestions(this.state.assessmentId)
    }

    componentDidUpdate(prevProps) {
        if (this.props.match.params.questionId !== prevProps.match.params.questionId) {
            // console.log("Component update with QId"+this.props.match.params.questionId)
            this.getNewQuestion(this.props.match.params.questionId)
        }
    }

    handleInputChange(event) {
        let newA
        const valuesSelected = Array
            .from(document.getElementsByName(this.inputName()))
            .filter((el) => el.checked)
            .map((el) => el.value);
        if (this.state.questionId in this.state.answersUser){
            newA=update(this.state.answersUser[this.state.questionId],valuesSelected)
        }
        else{
            newA=update(this.state.answersUser,{[this.state.questionId]:{$set: valuesSelected}})
        }
        this.setState({
            answersUser:newA,
            render:true,
        })
    }

    handleSubmit(event) {
        event.preventDefault();
        //console.log("Send submit ass="+this.state.assessmentId+'/question='+this.state.questionId)
        this.setState({
            isSaving:true
        })
        let closeAssessment=false
        if(event.target.name!==this.savingButtonName()){
            closeAssessment=true
        }
        const url='/assessment/'+this.state.assessmentId
        const aws=this.state.answersUser
        axios.put(url, {"answers": Array.from(aws.entries())},{headers: { 'Accept': 'application/json','Content-Type': 'application/json'}}).
        then(res => {if (res.status>300)
        {
            console.error("Error Getting data question ="+JSON.stringify(res))
        }
        else
        {
            if (closeAssessment){
                const url="/Report/"+this.state.assessmentId
                this.setState({
                    isLoaded:false,
                    redirect:true,
                    url:url,
                })
            }
        }
        this.setState({
            isSaving:false
        })
        })

    }

    preFillAnswers(question){
        let answers=question.Answers
        if (this.state.answersUser.has(this.state.questionId))
        {
            const preAnsw=this.state.answersUser.get(this.state.questionId)
            for (let i = 0; i < answers.length; i++) {
                answers[i].checked=preAnsw.includes(answers[i].placeHolder.charAt(0))
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
        const timePassed=this.TimerRef.current ? this.TimerRef.current.getTime() :0
        if(nextQ<parseInt(this.state.totalQuestions)){
            const url="/Question/"+this.state.assessmentId+"/"+nextQ
            this.setState({
                questionId:nextQ,
                url:url,
                TimerValue:timePassed,
                isLoaded:false,
                redirect:true,
            })
        }else if (nextQ===parseInt(this.state.totalQuestions))
        {
            const url="/Question/"+this.state.assessmentId+"/"+nextQ
            this.setState({
                questionId:nextQ,
                url:url,
                isLoaded:false,
                redirect:true,
                TimerValue:timePassed,
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
        const error=this.state.error
        const isLoaded=this.state.isLoaded
        const question=this.state.question
        if (this.state.redirect === true) {
            return <Redirect to={this.state.url} />
        }
        if (error) {
            return <div>Error: {error.message}</div>;
        } else if (!isLoaded || !this.state.render || !this.state.answersUser) {
            return <div>Loading...</div>;
        } else {
            // console.log("IsLoaded "+this.state.isLoaded)
            // console.log("rendering question state url="+JSON.stringify(this.state.question))
            this.preFillAnswers(question)
            const isOneAnswer=question.CorrectAnswers.length===1 ? "radio" : "checkbox"
            const leftEnable=this.state.questionId!=="1"
            const rightEnable=this.state.answersUser.has(this.state.questionId.toString()) && this.state.questionId<this.state.totalQuestions  ?
                this.state.answersUser.get(this.state.questionId.toString()).length>0 : false
            const percQuestions=Math.floor(this.state.question.Id/this.state.totalQuestions*100)
            const iconForTimer=this.state.TimerStatus==='PLAYING' ? pauseIcon: resumeIcon
            const submitEnable=this.state.answersUser.has(this.state.questionId) ?
                this.state.answersUser.get(this.state.questionId).length>0 : false
            const htmlText=parse(this.state.question.Text)
            return (
                <div className="form-group" >
                    <div className="container-fluid h-100 d-flex flex-column justify-content-center">
                         <div className="row ">
                            <div className="col ">
                                <h6 className="text-md-center"> Question N. {this.state.question.Id}/{this.state.totalQuestions}</h6>
                            </div>
                             <div className="col">
                                <div className="progress" style={{height: '30px'}}>
                                    <div className="progress-bar" role="progressbar" style={{width: percQuestions+'%'}}
                                         aria-valuenow={percQuestions} aria-valuemin="0" aria-valuemax="100" >{percQuestions}%</div>
                                </div>
                            </div>
                             <div className="col ml-lg-5">
                                 <Timer ref={this.TimerRef}
                                        initialTime={this.state.TimerValue}>
                                        <React.Fragment>
                                            <div className="input-group mb-3">
                                                <div className="input-group-prepend">
                                                    <span className="input-group-text" id="inputGroup-sizing-sm">
                                                        <div className={this.state.TimerValue>(36000*1000)?"visible":"invisible"}>
                                                            <Timer.Hours /> h
                                                        </div>
                                                    </span>
                                                    <span className="input-group-text" id="inputGroup-sizing-sm">
                                                    <Timer.Minutes className="m-3"/> m
                                                    </span>
                                                    <span className="input-group-text" id="inputGroup-sizing-sm">
                                                    <Timer.Seconds /> s
                                                    </span>
                                                </div>
                                                <div>
                                                    <button type="button" className="btn btn-light" data-toggle="button"
                                                            aria-pressed="false" onClick={this.onPauseResumeTime}>
                                                        <img className="img-thumbnail"  src={iconForTimer} alt="pause"/>
                                                    </button>
                                                </div>
                                            </div>
                                        </React.Fragment>
                                </Timer>
                             </div>
                    </div>
                        <div className="row ml-lg-0">
                                    <div className="card-header border-primary  border-5 ">
                                        <h5 className="card-text">
                                            {htmlText}
                                        </h5>
                                    </div>
                            </div>
                        <div className="card-body">
                            <div className="col m-3">
                                    {question.Answers.map(aws => (
                                        <div key={aws.placeHolder} className="form-check my-3">
                                        <input  className="form-check-input " name={this.inputName()} type={isOneAnswer} onClick={this.handleInputChange}
                                               value={aws.placeHolder} id={aws.placeHolder} defaultChecked={aws.checked}/>
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
                            <div className="col">
                                <button variant="secondary" disabled={this.state.isSaving} name={this.savingButtonName()} onClick={!this.state.isSaving ? this.handleSubmit : null} >
                                    {this.state.isSaving ? 'is Saving…' : 'Save Assessment'}</button>
                            </div>
                        </div>
                        <div className="row ml-lg-10">
                            <div className="col"/>
                            <div className="col m-5">
                                <div className={this.state.submitEnable?"visible":"invisible"}>
                                    <button className="btn btn-outline-primary" onClick={this.handleSubmit} disabled={!submitEnable}>Submit Answers</button>
                                </div>
                            </div>
                            <div className="col">

                            </div>
                        </div>
                    </div>

                </div>
            )
        }
    }
}

export default Question