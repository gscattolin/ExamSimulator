import React,{Component} from "react";

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
        const url='http://localhost:9000/assessment/'+assessmentId+'/report'
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

    componentDidMount() {
        this.getAssessmentReport(this.state.assessmentId)
    }

    // mapRepostResult(v){
    //     var cln="list-group-item list-group-item-"
    //     if (v.Correct) cln+="success" else cln+="danger"
    //
    //     return{
    //         "cln":cln
    //     }
    //
    // }


    render() {
        const lstReport=this.state.lstReport
        if (this.state.error) {
            return <div>Error: {this.state.error.message}</div>;
        } else if (!this.state.isLoaded) {
            return <div>Loading data......</div>;
        } else {
        //     const gReports=lstReport.map(rep =>{
        //
        //     }
        // )
            debugger
            return(
                <div className="container ">
                    <div className="row ml-lg-0">
                        <h2>Report and Results </h2>
                    </div>
                    <div className="row ml-lg-0">
                        <ul className="list-group">
                            {lstReport.map(rep=>(
                                <li key={rep.Id} className={"list-group-item list-group-item-"+(rep.Correct?"success":"danger")}>
                                    {rep.Id} . {rep.Text}
                                    <h5 className ="mb-1">Your Answer= {(rep.placeHolders.join(","))} </h5>
                                    <h5 className ="mb-1">Correct Answer= {(rep.correctPlaceHolders.join(","))} </h5>
                                    <label>{console.log(JSON.stringify(rep))}</label>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>

            )
        }
    }

}

export default Report