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
import clojure.lang.Util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static clojure.java.api.Clojure.read;
import static clojure.java.api.Clojure.var;

public final class HTTPCatalogClientImpl2 {

    static {
        IFn require = var("clojure.core", "require");
        require.invoke(read("eva.catalog.client.alpha.client"));
    }

    private static IFn requestFlatPeerConfigFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-config"
    );

    private static RuntimeException sneakyThrowIO(Throwable t) throws IOException {
        if(t instanceof IOException) {
            throw (IOException)t;
        } else {
            return Util.sneakyThrow(t);
        }
    }

    /**
     * Fetches a Peer configuration matching the tenant identifier, service category, and service label.
     *
     * @param address the catalog endpoint url
     * @param tenant the tenant identifier
     * @param category the service category name
     * @param label the service label name
     * @return Peer configuration Map
     * @throws java.net.UnknownHostException if the host of the address url cannot be resolved
     * @throws java.net.SocketTimeoutException if timeout occurs during connection or socket read/write operations
     * @throws IOException if an I/O error occurs
     */
    public static Map requestFlatPeerConfig(String address, String tenant, String category, String label) throws IOException  {
        try {
            return (Map) requestFlatPeerConfigFn.invoke(address, tenant, category, label);
        } catch(Throwable t) {
            throw sneakyThrowIO(t);
        }
    }

    private static IFn requestFlatPeerConfigsForTenantAndCategoryFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-for-tenant-and-category");

    /**
     * Fetches Peer configurations matching the tenant identifier and service category.
     *
     * @param address the catalog endpoint url
     * @param tenant the tenant identifier
     * @param category the service category name
     * @return Set of Peer configuration maps
     * @throws java.net.UnknownHostException if the host of the address url cannot be resolved
     * @throws java.net.SocketTimeoutException if timeout occurs during connection or socket read/write operations
     * @throws IOException if an I/O error occurs
     */
    public static Set requestFlatPeerConfigsForTenantAndCategory(String address, String tenant, String category) throws IOException {
        try {
            return (Set) (requestFlatPeerConfigsForTenantAndCategoryFn.invoke(address, tenant, category));
        } catch(Throwable t) {
            throw sneakyThrowIO(t);
        }
    }

    private static IFn requestFlatPeerConfigsForCategoryAndLabelFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-for-category-and-label"
    );

    /**
     * Fetches Peer configurations matching the service category and label.
     *
     * @param address the catalog endpoint url
     * @param category the service category name
     * @param label the service label name
     * @return Set of Peer configuration maps
     * @throws java.net.UnknownHostException if the host of the address url cannot be resolved
     * @throws java.net.SocketTimeoutException if timeout occurs during connection or socket read/write operations
     * @throws IOException if an I/O error occurs
     */
    public static Set requestFlatPeerConfigsForCategoryAndLabel(String address, String category, String label) throws IOException {
        try {
            return (Set)(requestFlatPeerConfigsForCategoryAndLabelFn.invoke(address, category, label));
        } catch(Throwable t) {
            throw sneakyThrowIO(t);
        }
    }

    private static IFn requestFlatConfigsInTransactorGroupFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-in-transactor-group"
    );

    /**
     * Fetches the configurations for databases assigned to the specified transactor(s).
     *
     * @param address the catalog endpoint url
     * @param transactorGroup the transactor name, or a common-separated string of multiple transactor-names
     * @return Set containing database configurations
     * @throws java.net.UnknownHostException if the host of the address url cannot be resolved
     * @throws java.net.SocketTimeoutException if timeout occurs during connection or socket read/write operations
     * @throws IOException if an I/O error occurs
     */
    public static Set requestFlatConfigsInTransactorGroup(String address, String transactorGroup) throws IOException {
        try {
            return (Set) (requestFlatConfigsInTransactorGroupFn.invoke(address, transactorGroup));
        } catch(Throwable t) {
            throw sneakyThrowIO(t);
        }
    }

    private static IFn requestFlatConfigsWithoutTransactorGroupFn = var(
            "eva.catalog.client.alpha.client",
            "request-flat-configs-without-transactor-group"
    );

    /**
     * Fetches database configurations for databases NOT assigned to any transactors.
     *
     * @param address the catalog endpoint url
     * @return Set of database configurations
     * @throws java.net.UnknownHostException if the host of the address url cannot be resolved
     * @throws java.net.SocketTimeoutException if timeout occurs during connection or socket read/write operations
     * @throws IOException if an I/O error occurs
     */
    public static Set requestFlatConfigsWithoutTransactorGroup(String address) throws IOException {
        try {
            return (Set) (requestFlatConfigsWithoutTransactorGroupFn.invoke(address));
        } catch(Throwable t) {
            throw sneakyThrowIO(t);
        }
    }

}
