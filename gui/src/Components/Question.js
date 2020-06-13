import React,{Component} from "react";
import {Redirect} from "react-router-dom";
import Timer from 'react-compound-timer'
import config from './config'


class Question extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        const now=Date.now()
        this.state = {
            assessmentId:props.match.params.assessmentId,
            questionId:props.match.params.questionId,
            isLoaded:false,
            render: true,
            redirect:false,
            submitEnable:false,
            startDate:now,
            timer:null,
            answersUser:[]
        }
    }

    // shouldComponentUpdate(nextProps, nextState, nextContext) {
        // if (nextProps.match.params.questionId!==this.props.match.params.questionId)
        // {
        //     console.log("shouldComponentUpdate next="+nextProps.match.params.questionId+" current"+this.props.match.params.questionId)
        //     this.setState({
        //         questionId:nextProps.match.params.questionId,
        //         isLoaded:false,
        //         redirect:false,
        //     })
        // }
    //     return true;
    // }

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
                    answersUser:[],
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

    componentDidMount()
    {
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
            this.getNewQuestion(this.props.match.params.questionId)
        }
    }

    handleInputChange(event) {
        const value = event.target.value;
        const a=this.state.answersUser
        if(event.target.checked){
            if (a.indexOf(value)<0){
                this.setState(previousState => ({
                    answersUser: [...previousState.answersUser, value],
                    submitEnable:true,
                }));
            }
        }else{
            if (a.indexOf(value)>-1){
                this.setState(prevState => {
                    const newA=prevState.answersUser.filter(a => a !==value)
                        return {
                            answersUser:newA,
                            submitEnable:newA.length>0,
                    }});
            }
        }

    }

    handleSubmit(event) {
        event.preventDefault();
        //console.log("Send submit ass="+this.state.assessmentId+'/question='+this.state.questionId)
        const url='http://localhost:9000/assessment/'+this.state.assessmentId+'/question/'+this.state.questionId
        const aws=this.state.answersUser
        fetch(url, {
            method: 'PUT',
            body: JSON.stringify({"answers": aws}),
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
            this.setState({redirect :true})
        }
        })

    }

    timeFromSec2Format(d){
        var h = Math.floor(d / 3600);
        var m = Math.floor(d % 3600 / 60);
        var s = Math.floor(d % 3600 % 60);
        return h+" h "+m+" min "+s+" s"
    }

    render() {
        const {error, isLoaded, question} = this.state;
        if (this.state.redirect === true) {
            // return <Redirect to={{pathname: '/Question',
            //     state:{ "assessmentId":this.state.assessmentId,"questionId":nextQ}}}/>

            const nextQ=parseInt(this.state.questionId)+1

            if(nextQ<=parseInt(this.state.totalQuestions)){
                const nextQ=parseInt(this.state.questionId)+1
                const url="/Question/"+this.state.assessmentId+"/"+nextQ
                return <Redirect to={url} />
            }else
            {
                //report
                const url="/Report/"+this.state.assessmentId
                return <Redirect to={url} />
            }
        }
        if (error) {
            return <div>Error: {error.message}</div>;
        } else if (!isLoaded) {
            return <div>Loading...</div>;
        } else {
            // console.log("rendering question state="+JSON.stringify(this.state))
            const answers = question.Answers
            var timePassed=0
            if (this.state.timer){
                // sec=this.state.timer.getSeconds()
                // min=this.state.timer.getMinutes()
                // hours=this.state.timer.getHours()
                timePassed=this.timeFromSec2Format(this.state.timer/1000)
            }

            return (
                <form onSubmit={this.handleSubmit}>
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
                                        {answers.map(aws => (
                                            <div key={aws.placeHolder} className="form-check my-3">
                                            <input  className="form-check-input " type="checkbox"  onChange={this.handleInputChange}
                                                   value={aws.placeHolder} id={aws.placeHolder}/>
                                                <label className="form-check-label " htmlFor={aws.placeHolder}>
                                                    {aws.Text}
                                                </label>
                                            </div>
                                        ))}
                                </div>
                            </div>
                            <div className="row ml-lg-0">
                                <div className="col"/>
                                <div className="col">
                                    <button type="submit" className="btn btn-primary" disabled={!this.state.submitEnable}>Submit Answer</button>
                                </div>
                                <div className="col"/>
                            </div>
                        </div>
                    </div>
                </form>
            )
        }
    }
}

export default Question