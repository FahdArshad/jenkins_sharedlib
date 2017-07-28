package utils;

import java.nio.charset.StandardCharsets

//************************************************************************
// Function   : countStages
// Purpose    : count the number of stages present inside the jenkinsfile
// Usage      : countStages(jenkinfile config)
// Parameters : contents of jenkinsfile
// Return     : number of stages
// ***********************************************************************
@NonCPS
def countStages(def args)
{
   def count = 0
   def li = stageList(args)
   count = li.size()
   return count;
}

//************************************************************************
// Function   : stageList
// Purpose    : convert jenkinsfile contents into a list
// Usage      : stageList(jenkinfile config)
// Parameters : contents of jenkinsfile
// Return     : list of jenkinsfile data
// ***********************************************************************
@NonCPS
def stageList(def args)
{
   def list = []
   for(emp in args) {
      if (emp.toString().contains('stage'))
        {
          list.add("$emp")
        }
    }
   return list;
}

def ticketGeneration()
{
  @Grab(group='org.apache.commons', module='commons-io', version='1.3.2')
  def cfg_file = readFile('config.ini')
  def config = new ConfigSlurper().parse(cfg_file)
  def bugzilla_server_ip = config.bugzilla_ip
  echo "$bugzilla_server_ip"

}

//************************************************************************
// Function   : prepareStages
// Purpose    : calls a function that creates stages using a list
// Usage      : prepareStages(number,config)
// Parameters : stage number, and jenkinsfile data
// Return     : None
// ***********************************************************************
@NonCPS
def prepareStages(def iter, def args)
{
   def li = stageList(args)
   def cmd = li[iter]
   def (fullstage, command) = cmd.split('=')
   def (key, stage_name) = fullstage.split('_')
   createStages(stage_name,command)
}

//************************************************************************
// Function   : createStages
// Purpose    : creating stages dynamically and excuting the shell command
// Usage      : createStages(stage_name,command)
// Parameters : name of the stage, command to run in stage
// Return     : None
// ***********************************************************************
def createStages(def stage_name, def command)
{
  stage ("$stage_name")
  {
    sh "$command"
  }
}

//************************************************************************
// Function   : archive_artifacts
// Purpose    : archives artifacts on to Alfred master
// Usage      : archive_artifacts(dir)
// Parameters : dir for artifacts specified in config file
// Return     : None
// ***********************************************************************
def archive_artifacts(def dir)
{
   // Artifacts to be archived for Alfred Master
   archiveArtifacts allowEmptyArchive: true, artifacts: dir
}

def postPipeline(def args, String buildStatus)
{
  //alfredInfo(args)
  archive_artifacts(args.artifacts)
  emailAlert(buildStatus, args.email)
}

//def alfredInfo(def args)
//{
//  echo "$args.artifacts"
//  dir (args.artifacts)
//  { 
//      def contents ="machine_arch = $args.machine_arch \n" + 
//                    "build_url = ${BUILD_URL}"  
     
//      writeFile file: 'alfred.info', text: "$contents"
//  }
//}

def emailAlert(String build_result,owners)
{
   if (owners)
  {
    def subject = "[Alfred] $JOB_NAME (# $BUILD_NUMBER)  - $build_result!"
    def body = "Hi Team,\n\n" +
                "Pipeline: ${JOB_NAME}\n" +
                "\n" +
                "Build number: ${BUILD_NUMBER}\n" +
                "\n" +
                "Build status: ${build_result}\n" +
                "\n" +
                "Console output: ${BUILD_URL}\n" +
                "\n" +
                "--\n" +
                "Alfred"

    mail body: "$body", subject: "$subject", to: "$owners"
  }
  else
  {
    echo "No email Recipents Sepecified!"
  }
}
return this;
;
