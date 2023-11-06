/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.component.bpmn.lambda;

import java.io.Serializable;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
public interface LambdaParam {

    @FunctionalInterface
    interface LambdaParam0<Component> extends Serializable {

        void param0(Component component);
    }

    @FunctionalInterface
    interface LambdaParam1<Component, A> extends Serializable {

        void param1(Component component, A a);
    }

    @FunctionalInterface
    interface LambdaParam2<Component, A, B> extends Serializable {

        void param2(Component component, A a, B b);
    }

    @FunctionalInterface
    interface LambdaParam3<Component, A, B, C> extends Serializable {

        void param3(Component component, A a, B b, C c);
    }

    @FunctionalInterface
    interface LambdaParam4<Component, A, B, C, D> extends Serializable {

        void param4(Component component, A a, B b, C c, D d);
    }

    @FunctionalInterface
    interface LambdaParam5<Component, A, B, C, D, E> extends Serializable {

        void param5(Component component, A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    interface LambdaParam6<Component, A, B, C, D, E, F> extends Serializable {

        void param6(Component component, A a, B b, C c, D d, E e, F f);
    }

    @FunctionalInterface
    interface LambdaParam7<Component, A, B, C, D, E, F, G> extends Serializable {

        void param7(Component component, A a, B b, C c, D d, E e, F f, G g);
    }

    @FunctionalInterface
    interface LambdaParam8<Component, A, B, C, D, E, F, G, H> extends Serializable {

        void param8(Component component, A a, B b, C c, D d, E e, F f, G g, H h);
    }

    @FunctionalInterface
    interface LambdaParam9<Component, A, B, C, D, E, F, G, H, I> extends Serializable {

        void param9(Component component, A a, B b, C c, D d, E e, F f, G g, H h, I i);
    }

    @FunctionalInterface
    interface LambdaParam10<Component, A, B, C, D, E, F, G, H, I, J> extends Serializable {

        void param10(Component component, A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);
    }

    @FunctionalInterface
    interface LambdaParamR0<Component> extends Serializable {

        Object paramR0(Component component);
    }

    @FunctionalInterface
    interface LambdaParamR1<Component, A> extends Serializable {

        Object paramR1(Component component, A a);
    }

    @FunctionalInterface
    interface LambdaParamR2<Component, A, B> extends Serializable {

        Object paramR2(Component component, A a, B b);
    }

    @FunctionalInterface
    interface LambdaParamR3<Component, A, B, C> extends Serializable {

        Object paramR3(Component component, A a, B b, C c);
    }

    @FunctionalInterface
    interface LambdaParamR4<Component, A, B, C, D> extends Serializable {

        Object paramR4(Component component, A a, B b, C c, D d);
    }

    @FunctionalInterface
    interface LambdaParamR5<Component, A, B, C, D, E> extends Serializable {

        Object paramR5(Component component, A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    interface LambdaParamR6<Component, A, B, C, D, E, F> extends Serializable {

        Object paramR6(Component component, A a, B b, C c, D d, E e, F f);
    }

    @FunctionalInterface
    interface LambdaParamR7<Component, A, B, C, D, E, F, G> extends Serializable {

        Object paramR7(Component component, A a, B b, C c, D d, E e, F f, G g);
    }

    @FunctionalInterface
    interface LambdaParamR8<Component, A, B, C, D, E, F, G, H> extends Serializable {

        Object paramR8(Component component, A a, B b, C c, D d, E e, F f, G g, H h);
    }

    @FunctionalInterface
    interface LambdaParamR9<Component, A, B, C, D, E, F, G, H, I> extends Serializable {

        Object paramR9(Component component, A a, B b, C c, D d, E e, F f, G g, H h, I i);
    }

    @FunctionalInterface
    interface LambdaParamR10<Component, A, B, C, D, E, F, G, H, I, J> extends Serializable {

        Object paramR10(Component component, A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);
    }

    @FunctionalInterface
    interface LambdaSubProcess<Link> extends Serializable {

        Object process(Link link);
    }

    @FunctionalInterface
    interface LambdaProcess<Link> extends Serializable {

        Object process(Link link);
    }
}
