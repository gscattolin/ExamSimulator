import React,{Component} from "react";
import {Redirect} from "react-router-dom";



class Question extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.state = {
            assessmentId:props.match.params.assessmentId,
            questionId:props.match.params.questionId,
            isLoaded:false,
            render: true,
            redirect:false,
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

    getNewQuestion(questionId){
        this.setState({isLoaded:false})
        // console.log("Getting data question"+questionId)
        const url='http://localhost:9000/assessment/'+this.state.assessmentId+'/question/'+questionId
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
        const url='http://localhost:9000/assessment/'+assessmentId+'/question'
        fetch(url)
            .then(res => res.json()).then(data => {
                this.setState({
                        error: null,
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
        this.getNewQuestion(1)
        this.getTotalQuestions(this.state.assessmentId)
    }

    componentDidUpdate(prevProps) {
        if (this.props.match.params.questionId !== prevProps.match.params.questionId) {
            console.log("componentDidUpdate "+this.props.match.params.questionId)
            this.getNewQuestion(this.props.match.params.questionId)
        }
    }

    handleInputChange(event) {
        const value = event.target.value;
        const a=this.state.answersUser
        if(event.target.checked){
            if (a.indexOf(value)<0){
            this.state.answersUser.push(value);
            }
        }else{
            if (a.indexOf(value)<0){
                this.state.answersUser.splice(a.indexOf(value),1);
            }
        }

    }

    handleSubmit(event) {
        event.preventDefault();
        console.log("Send submit ass="+this.state.assessmentId+'/question='+this.state.questionId)
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
            return (
                <form onSubmit={this.handleSubmit}>
                    <div className="form-group" >
                        <div className="container-fluid ">
                             <div className="row my-lg-2">
                            <div className="col"/>
                            <div className="col">
                                <h2>Question N. {this.state.question.Id}/{this.state.totalQuestions}</h2>
                            </div>
                                <div className="col"/>
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
                                    <button type="submit" className="btn btn-primary">Submit Answer</button>
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