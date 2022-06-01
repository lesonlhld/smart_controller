@echo off
@prompt set date=$d$_ set time=$t$h$h$h
set LOG=updating files - %date% %time%

git commit -a -m "%LOG%"
git push -u origin master