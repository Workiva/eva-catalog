// Copyright 2018-2019 Workiva Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package eva.catalog.client.alpha;

import clojure.lang.IFn;

import java.util.Map;
import java.util.Set;

import static clojure.java.api.Clojure.read;
import static clojure.java.api.Clojure.var;

public final class HTTPCatalogClientImpl {

    static {
        IFn require = var("clojure.core", "require");
        require.invoke(read("eva.catalog.client.alpha.client"));
    }

    private static IFn requestFlatPeerConfigFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-config"
    );

    public static Map requestFlatPeerConfig(String address, String tenant, String category, String label) {
        return (Map)requestFlatPeerConfigFn.invoke(address, tenant, category, label);
    }

    private static IFn requestFlatPeerConfigsForTenantAndCategoryFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-for-tenant-and-category");

    public static Set requestFlatPeerConfigsForTenantAndCategory(String address, String tenant, String category) {
        return (Set)(requestFlatPeerConfigsForTenantAndCategoryFn.invoke(address, tenant, category));
    }

    private static IFn requestFlatPeerConfigsForCategoryAndLabelFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-for-category-and-label"
    );

    public static Set requestFlatPeerConfigsForCategoryAndLabel(String address, String category, String label) {
        return (Set)(requestFlatPeerConfigsForCategoryAndLabelFn.invoke(address, category, label));
    }

    private static IFn requestFlatConfigsInTransactorGroupFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-in-transactor-group"
    );

    public static Set requestFlatConfigsInTransactorGroup(String address, String transactorGroup) {
        return (Set)(requestFlatConfigsInTransactorGroupFn.invoke(address, transactorGroup));
    }

    private static IFn requestFlatConfigsWithoutTransactorGroupFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-without-transactor-group"
    );

    public static Set requestFlatConfigsWithoutTransactorGroup(String address) {
        return (Set)(requestFlatConfigsWithoutTransactorGroupFn.invoke(address));
    }

}
