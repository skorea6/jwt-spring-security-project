#!/bin/bash
cd /var/jenkins_home/custom/jwt

DOCKER_APP_NAME=spring-jwt
LOG_FILE=./deploy.log

echo "배포 시작일자 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> $LOG_FILE
echo "green 중단 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> $LOG_FILE

docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml down

docker image prune -af

echo "green 중단 완료 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> $LOG_FILE

sleep 10

echo "green 배포 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> $LOG_FILE
docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml up -d --build

echo "배포 종료  : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> $LOG_FILE

echo "===================== 배포 완료 =====================" >> $LOG_FILE
echo >> $LOG_FILE