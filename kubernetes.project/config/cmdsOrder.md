# 1. Criar imagem Docker e registar no repositório Docker
docker pull mongodb
docker login
docker build -t scc2223-app {filepath_to_war_file}
docker tag scc2223-app {docker_id}/{docker_repo}
docker push {docker_id}/{docker_repo}

# 2. Executar o batch createAppContainer, alterando o resource group (da Azure)
az container create --resource-group {azure_resource_group} --name {docker_repo} --image {docker_id}/{docker_repo} --ports 8080 --dns-name-label {domain_label} --restart-policy OnFailure

# 3. Obter credenciais de acesso ao cluster
az aks get-credentials --resource-group {azure_resource_group} --name {cluster_name}

# 4. Criar serviços
kubectl apply -f mongo-config.yaml
kubectl apply -f azure-app.yaml

# Extras
* Para criar um service principal (necessário para criar clusters)
az ad sp create-for-rbac --name http://scc2223-app --role Contributor --scope /subscriptions/{subscription_id}

* Para criar o cluster (substituir principal_id e principal_password)
az aks create --resource-group {azure_resource_group} --name {cluster_name} --node-vm-size Standard_B2s --generate-ssh-keys --node-count 2 --service-principal {principal_appid} --client-secret {principal_password}

* Para aceder ao PostgreSQL (pass -> test):
kubectl exec -it postgres-5cb8b67d8f-jn9xs --  psql -h localhost -U scc-app --password -p 5432 postgresdb

* Comandos extra:
kubectl get services
kubectl get pods
kubectl get configmap
kubectl get pvc
kubectl get deployments
kubectl get all
kubectl delete deployments,services,pods --all
kubectl delete pv --all
az group delete --resource-group {azure_resource_group}