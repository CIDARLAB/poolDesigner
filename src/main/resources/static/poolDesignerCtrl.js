function poolDesignerCtrl($scope) {
    $scope.searchSpaceID = "";
    $scope.deleteSpaceID = "";

    $scope.specNodes = [];
    $scope.poolNodes = [];

    $scope.specNode = function(pool) {
        this.pool = pool;
        this.labelColor = "";
        this.labelTxt = "Pool Structure";
        this.backgroundColor = "";
    };

    $scope.poolNode = function(pool) {
        this.pool = pool;
        this.labelColor = "color:#ffffff";
        this.labelTxt = "Pool";
        this.backgroundColor = "background-color:#787878";
    };

    $scope.moveNodeToTop = function(node) {
        if (node.labelColor.length > 0) {
            $scope.poolNodes.splice($scope.specNodes.indexOf(node), 1);

            $scope.poolNodes.unshift(node);
        } else {
            $scope.specNodes.splice($scope.specNodes.indexOf(node), 1);

            $scope.specNodes.unshift(node);
        }
    };

    $scope.poolTreeOptions = {
        accept: function(sourceNodeScope, destNodesScope, destIndex) {
            return destNodesScope.$id === sourceNodeScope.$parentNodesScope.$id;
        }
    };

    $scope.createSpecNode = function() {
         $scope.specNodes.push(new $scope.specNode("[scar][promoter][RBS][CDS][terminator][scar]"));
    }

    $scope.removeNode = function(node) {
        node.remove();
    };

    $scope.designPools = function() {
        var poolSpecs = [];

        var i;

        for (i = 0; i < $scope.specNodes.length; i++) {
            poolSpecs[i] = $scope.specNodes[i].pool;
        }

        d3.json("/design/pool").post(JSON.stringify(poolSpecs),
            function(error, result) {
                $scope.$apply(function () {
                    if (error) {
                        sweetAlert("Error", error.target.response, "error");
                    } else {
                        var i;

                        for (i = 0; i < result.length; i++) {
                            $scope.poolNodes[i] = new $scope.poolNode(result[i]);
                        }
                    }
                });
            }
        );
    }

    $scope.deleteAll = function() {
        d3.xhr("/delete/all").post(function(error, request) {
            if (error) {
                sweetAlert("Error", JSON.parse(error.response).message, "error");
            }
        });
    };

}