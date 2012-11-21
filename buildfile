define 'wca-workbook-uploader' do
  project.version = '0.2'

  compile.with(Dir[_("lib/*.jar")])

  package(:jar).with :manifest=>{ 'Main-Class'=>'org.worldcubeassociation.WorkbookUploader' }

  package :jar
end
