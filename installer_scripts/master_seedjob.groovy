#!groovy

// Load default configurations
@Grab(group='org.apache.commons', module='commons-io', version='1.3.2')
def cfg_file = readFileFromWorkspace('config.ini')
def config = new ConfigSlurper().parse(cfg_file)

// Get all contents of Alfred enabled list
def alfred_list = readFileFromWorkspace('alfred_enabled.list')
String[] split_file = alfred_list.split(System.getProperty("line.separator"));
def alfredRepo = config.git_url + "alfred.git"
def branch_map = [:]
def url_map = [:]

// Creating a map for repos against branches
for (def line:split_file)
{
  String[] line_split = line.split(" ")
  repo = line_split.getAt(0)
  branch = line_split.getAt(1)
  url = line_split.getAt(2)
  branch_map[repo] = branch
  url_map[repo] = url
}

// Iterating over each repo inside the map
for ( project in branch_map.keySet() )
{
    // Creating a freestylejob that acts as a seedjob for each repo
    freeStyleJob("${project}-seedjob")
    {
        // Restrict pipeline to run on master only
        label('master')

        // Injecting Environment variables to be used by Alfred
        environmentVariables
        {
            env('PROJECT', project)
            env('PROJECTURL', url_map[project])
            env('BRANCH', branch_map[project])
        }

        // Pre build workspace cleanup
        wrappers
        {
            preBuildCleanup()
        }

        // Adding Alfred repo as default SCM for seedjob
        scm
        {
            git
            {
                remote
                {
                    credentials('jenkins')
                    url(url_map[project])
                }
                branch ('master')
                extensions
                {
                    relativeTargetDirectory(project)
                }
            }
        }

        triggers
        {
            scm('H/05 * * * *')
        }

        // Groovy script to be called inside the seedjob
        steps
        {
            if(project != "alfred")
            {
                shell('git clone ' + alfredRepo)
            }
            dsl
            {
                external('alfred/groovy_scripts/template_seedjob.groovy')
            }
        }
    }
}
