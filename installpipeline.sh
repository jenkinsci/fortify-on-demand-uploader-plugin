hoststr=localhost:8080
javaexe=''

if [ $# != 0 ]
then
  prevArg=''
  for a in "$@"
  do
      if [ $a == '-h' ]
      then
        prevArg='-h'
      elif [ $a == '-j' ]
      then
        prevArg='-j'
      elif [ $prevArg == '-h' ]
      then
        hoststr=$a
      elif [ $prevArg == '-j' ]
      then
        javaexe=$a
      fi
  done
fi

if [[ $javaexe == "" ]]
then
  echo 'Must provide java path'
  echo ''
  echo 'Example:'
  echo -e '\t./installpipeline.sh -j ~/.jdks/corretto-1.8.0_402/bin/java.exe'
  exit 1
fi


echo Host: $hoststr
echo Java: $javaexe


$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-milestone-step/1.3.1/pipeline-milestone-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-basic-steps/2.11/workflow-basic-steps.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-build-step/2.7/pipeline-build-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-input-step/2.8/pipeline-input-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-multibranch/2.20/workflow-multibranch.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/cloudbees-folder/6.6/cloudbees-folder.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-cps-global-lib/2.11/workflow-cps-global-lib.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/scm-api/2.2.8/scm-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-support/2.20/workflow-support.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-api/2.29/workflow-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/git-client/2.7.3/git-client.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-scm-step/2.6/workflow-scm-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-stage-step/2.3/pipeline-stage-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-cps/2.56/workflow-cps.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-job/2.25/workflow-job.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-model-definition/1.3.2/pipeline-model-definition.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/lockable-resources/2.3/lockable-resources.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/workflow-durable-task-step/2.22/workflow-durable-task-step.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/ssh-credentials/1.13/ssh-credentials.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/jsch/0.1.54.1/jsch.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/matrix-project/1.4/matrix-project.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/apache-httpcomponents-client-4-api/4.5.5-3.0/apache-httpcomponents-client-4-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/mailer/1.20/mailer.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/ace-editor/1.0.1/ace-editor.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/jquery-detached/1.2.1/jquery-detached.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/durable-task/1.26/durable-task.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/display-url-api/1.0/display-url-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/git-server/1.7/git-server.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/branch-api/2.0.18/branch-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/credentials-binding/1.13/credentials-binding.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-stage-tags-metadata/1.3.2/pipeline-stage-tags-metadata.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/docker-workflow/1.14/docker-workflow.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-model-api/1.3.2/pipeline-model-api.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-model-extensions/1.3.2/pipeline-model-extensions.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/pipeline-model-declarative-agent/1.1.1/pipeline-model-declarative-agent.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/docker-commons/1.5/docker-commons.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/authentication-tokens/1.1/authentication-tokens.hpi && \
$javaexe -jar jenkins-cli.jar -s http://$hoststr/jenkins/ -webSocket install-plugin https://updates.jenkins.io/download/plugins/icon-shim/1.0.3/icon-shim.hpi