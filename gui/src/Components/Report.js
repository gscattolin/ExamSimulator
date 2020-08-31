import React,{Component} from "react";
import { Link } from 'react-router-dom'

class Report extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isLoaded:false,
            assessmentId: props.match.params.assessmentId
        }
    }

    getAssessmentReport(assessmentId){
        // console.log("Getting data question"+questionId)
        const url='/assessment/'+assessmentId+'/report'
        fetch(url)
            .then(res => res.json()).then(data => {
                this.setState({
                        error: null,
                        isLoaded:true,
                        lstReport: data,
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

    getAssessmentTime(assessmentId){
        const url='/assessment/'+assessmentId+'/info/1'
        fetch(url)
            .then(res => res.json()).then(data => {
                this.setState({
                        error: null,
                        timeInSec: data['timeinseconds'],
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

    timeFromSec2Format(d){
        var h = Math.floor(d / 3600);
        var m = Math.floor(d % 3600 / 60);
        var s = Math.floor(d % 3600 % 60);
        if (h>0)
            return h+" h "+m+" min "+s+" s"
        else
            return m+" min "+s+" s"
    }

    FloorDigit(v,n){
        return Math.floor(v*10**n)/10**n
    }

    componentDidMount() {
        this.getAssessmentTime(this.state.assessmentId)
        this.getAssessmentReport(this.state.assessmentId)
    }


    render() {
        const lstReport=this.state.lstReport
        if (this.state.error) {
            return <div>Error: {this.state.error.message}</div>;
        } else if (!this.state.isLoaded) {
            return <div>Loading data......</div>;
        } else {
            const nCorrectQ=lstReport.filter((item) => item.Correct).length
            const passedTime=this.timeFromSec2Format(this.state.timeInSec)
            return(
                <div className="container ">
                    <div className="row ml-lg-0">
                        <h2>Report and Results </h2>
                    </div>
                    <div className="row ml-lg-0">
                        <div className="col-sm">
                            Total Correct/Total Questions={nCorrectQ}/{lstReport.length}
                        </div>
                        <div className="col-sm">
                            % Total Correct={this.FloorDigit(nCorrectQ/lstReport.length*100,2)}%
                        </div>
                        <div className="col-sm">
                            Time Spent={passedTime}
                        </div>
                    </div>
                    <div className="row ml-lg-0">
                        <ul className="list-group">
                            {lstReport.map(rep=>(
                                <li key={rep.Id} className={"list-group-item list-group-item-"+(rep.Correct?"success":"danger")}>
                                    <b>{rep.Id} . {rep.Text}</b>
                                    <ul className="list-group">
                                    {rep.Answers.map( a=>
                                        <li className="list-group-item list-group-item-info" key={a.charAt(0)}>{a}</li>
                                    )}
                                    </ul>
                                    <h5 className ="mb-1">Your Answer= {(rep.placeHolders.join(","))} </h5>
                                    <h5 className ="mb-1">Correct Answer= {(rep.correctPlaceHolders.join(","))} </h5>
                                    <h6>{rep.Explanation}</h6>
                                </li>
                            ))}
                        </ul>
                    </div>
                    <div className="row ml-lg-5">
                        <Link to={`/start`}>
                            <button type="button" className="btn btn-info">Back to Main</button>
                        </Link>
                    </div>
                </div>
            )
        }
    }

}

export default Report