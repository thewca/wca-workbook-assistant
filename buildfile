define 'wca-workbook-assistant' do
  project.version = '0.2'

  compile.with(Dir[_("lib/*.jar")])

  package(:jar).with :manifest=>{ 'Main-Class'=>'org.worldcubeassociation.WorkbookAssistant' }

  package :jar
end
