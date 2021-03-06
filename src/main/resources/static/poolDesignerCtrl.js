function poolDesignerCtrl($scope) {
    $scope.searchSpaceID = "";
    $scope.deleteSpaceID = "";

    $scope.specNodes = [];
    $scope.poolNodes = [];

    $scope.isReverseIncluded = {
        isChecked: true
    };

    $scope.SpecNode = function(pool) {
        this.pool = pool;
        this.labelColor = "";
        this.labelTxt = "Pool Structure";
        this.txtDim = "height:25px;width:500px";
        this.backgroundColor = "";
    };

    $scope.SpecNode.prototype.parsePool = function() {
        return JSON.parse("[" + this.pool.replace(/\[/g, "[\"").replace(/\]/g, "\"]").replace(/,/g, "\",\"").replace(/\]\[/g, "],[") + "]");
    };

    $scope.SpecNode.prototype.removeWhiteSpace = function() {
        this.pool = this.pool.replace(/\s+/g, "");
    };

    $scope.SpecNode.prototype.reversePool = function() {
        var poolArr = this.parsePool();

        var subArrLengths = [];

        var i, j;

        for (i = poolArr.length - 1; i >= 0; i--) {
            subArrLengths[i] = poolArr[i].length;
        }

        for (i = poolArr.length - 1; i >= 0; i--) {
            for (j = 0; j < subArrLengths[i]; j++) {
                if (poolArr[i][j].startsWith("r^")) {
                    poolArr[poolArr.length - 1 - i].push(poolArr[i][j].slice(3));
                } else {
                    poolArr[poolArr.length - 1 - i].push("r^" + poolArr[i][j]);
                }
            }
        }

        var poolWithReverse = JSON.stringify(poolArr);

        return poolWithReverse.slice(1, poolWithReverse.length - 1).replace(/\[\"/g, "[").replace(/\"\]/g, "]").replace(/\",\"/g, ",").replace(/\],\[/g, "][");
    };

    $scope.PoolNode = function(pool) {
        this.pool = pool.replace(/\s+/g, "");
        this.altPool = pool.replace(/\s+/g, "");
        this.labelColor = "color:#ffffff";
        this.labelTxt = "Pool";
        this.txtDim = "height:25px;width:1000px";
        this.backgroundColor = "background-color:#787878";
    };

    $scope.PoolNode.prototype.parsePool = function() {
        return JSON.parse("[" + this.pool.replace(/\[/g, "[\"").replace(/\]/g, "\"]").replace(/,/g, "\",\"").replace(/\]\[/g, "],[") + "]");
    };

    $scope.PoolNode.prototype.togglePool = function() {
        if (this.pool === this.altPool) {
            var poolArr = this.parsePool();

            var shortPoolArr = [];

            var i, j;

            for (i = 0; i < poolArr.length; i++) {
                shortPoolArr[i] = [];

                for (j = 0; j < poolArr[i].length; j++) {
                    shortPoolArr[i][j] = poolArr[i][j].trim().slice(Math.max(poolArr[i][j].lastIndexOf("/"), poolArr[i][j].lastIndexOf("#"), poolArr[i][j].lastIndexOf(":")) + 1);
                
                    if (poolArr[i][j].startsWith("r^")) {
                        shortPoolArr[i][j] = "r^" + shortPoolArr[i][j];
                    }
                }
            }

            this.pool = JSON.stringify(shortPoolArr);

            this.pool = this.pool.slice(1, this.pool.length - 1).replace(/\[\"/g, "[").replace(/\"\]/g, "]").replace(/\",\"/g, ",").replace(/\],\[/g, "][");
        } else {
            var tempPool = this.pool;

            this.pool = this.altPool;

            this.altPool = tempPool;
        }
    };

    $scope.togglePoolNode = function(node) {
        node.togglePool();
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
         $scope.specNodes.push(new $scope.SpecNode("[scar][promoter][RBS][CDS][terminator][scar]"));
    }

    $scope.removeNode = function(node) {
        node.remove();
    };

    $scope.designPools = function() {
        var poolSpecs = [];

        var i;

        for (i = 0; i < $scope.specNodes.length; i++) {
            $scope.specNodes[i].removeWhiteSpace();

            if ($scope.isReverseIncluded.isChecked) {
                poolSpecs[i] = $scope.specNodes[i].reversePool();
            } else {
                poolSpecs[i] = $scope.specNodes[i].pool;
            }
        }

        d3.json("/design/pool").post(JSON.stringify(poolSpecs),
            function(error, result) {
                $scope.$apply(function () {
                    if (error) {
                        sweetAlert("Error", error.target.response, "error");
                    } else {
                        var i;

                        for (i = 0; i < result.length; i++) {
                            $scope.poolNodes.push(new $scope.PoolNode(result[i]));

                            $scope.poolNodes[$scope.poolNodes.length - 1].togglePool();
                        }
                    }
                });
            }
        );
    }

    $scope.deleteAll = function() {
        d3.json("/delete/all").post("",
            function(error, result) {
                if (error) {
                    sweetAlert("Error", error, "error");
                } else {
                    sweetAlert("Success", result, "success");
                }
        });
    };

    $scope.downloadPools = function() {
        var content = "";

        var i;

        for (i = 0; i < $scope.poolNodes.length; i++) {
            content += $scope.poolNodes[i].pool;
        }

        content = content.replace(/\]\[/g, "\n").replace(/\[/g, "").replace(/\]/g, "");

        var blob = new Blob([content]);

        if (window.navigator.msSaveOrOpenBlob) {  // IE hack; see http://msdn.microsoft.com/en-us/library/ie/hh779016.aspx
            window.navigator.msSaveBlob(blob, "pool_designs.csv");
        } else {
            var link = window.document.createElement("a");

            link.href = window.URL.createObjectURL(blob, {type: "text/plain"});

            link.download = "pool_designs.csv";

            document.body.appendChild(link);

            link.click();  // IE: "Access is denied"; see: https://connect.microsoft.com/IE/feedback/details/797361/ie-10-treats-blob-url-as-cross-origin-and-denies-access
            
            document.body.removeChild(link);
        }
    };
}